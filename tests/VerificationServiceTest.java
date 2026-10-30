package ub.dormhub.tests;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import ub.dormhub.adapters.InMemoryDemoStore;
import ub.dormhub.application.VerificationService;
import ub.dormhub.domain.MaintenanceComplaint;
import ub.dormhub.ports.*;

/** Self-contained no-dependency automated tests. Run via scripts/test.sh or test.ps1. */
public final class VerificationServiceTest {
    private static int passed = 0;
    private record Fixture(InMemoryDemoStore store, VerificationService service) { }
    private static Fixture fixture() {
        InMemoryDemoStore s = new InMemoryDemoStore();
        return new Fixture(s, new VerificationService(s,s,s,s,s));
    }
    private static VerificationCommand command(long id, VerificationCommand.Decision decision, String reason, String key, long user) {
        return new VerificationCommand(id,decision,reason,key,user);
    }
    private static String key() { return UUID.randomUUID().toString(); }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    private static void expect(String token, Runnable task) {
        try { task.run(); throw new AssertionError("Expected error containing " + token); }
        catch (RuntimeException ex) {
            check(ex.getMessage() != null && ex.getMessage().contains(token), "Wrong error: " + ex);
        }
    }
    private static void run(String name, Runnable task) {
        task.run(); passed++; System.out.println("PASS " + name);
    }
    private static MaintenanceComplaint.Status status(Fixture f, long id) {
        return f.store.findById(id).orElseThrow().status();
    }
    public static void main(String[] args) throws Exception {
        run("verify creates certification", () -> {
            Fixture f=fixture(); VerificationResult r=f.service.execute(command(1042,VerificationCommand.Decision.VERIFY,null,key(),2));
            check("VERIFIED".equals(r.status()) && r.certificationId()!=null, "wrong result");
            check(status(f,1042)==MaintenanceComplaint.Status.VERIFIED, "wrong status");
            check(f.store.certificationCount()==1, "no certification");
        });
        run("reject records reason without certification", () -> {
            Fixture f=fixture(); f.service.execute(command(1042,VerificationCommand.Decision.REJECT,"Fault belongs to another room",key(),2));
            check(status(f,1042)==MaintenanceComplaint.Status.REJECTED, "not rejected");
            check(f.store.certificationCount()==0, "rejection must not certify");
            check("Fault belongs to another room".equals(f.store.findById(1042).orElseThrow().rejectionReason()), "reason absent");
        });
        run("reject validates minimum reason", () -> {
            Fixture f=fixture(); expect("VALIDATION_FAILED",()->f.service.execute(command(1042,VerificationCommand.Decision.REJECT,"Wrong",key(),2)));
            check(status(f,1042)==MaintenanceComplaint.Status.REPORTED,"changed invalid input");
        });
        run("reject validates maximum reason", () -> {
            Fixture f=fixture(); expect("VALIDATION_FAILED",()->f.service.execute(command(1042,VerificationCommand.Decision.REJECT,"x".repeat(501),key(),2)));
        });
        run("role enforcement denies student", () -> {
            Fixture f=fixture(); expect("FORBIDDEN_ROLE",()->f.service.execute(command(1042,VerificationCommand.Decision.VERIFY,null,key(),1)));
            check(f.store.certificationCount()==0,"student wrote data");
        });
        run("same key same request replay", () -> {
            Fixture f=fixture(); String k=key(); VerificationCommand c=command(1042,VerificationCommand.Decision.VERIFY,null,k,2);
            VerificationResult a=f.service.execute(c), b=f.service.execute(c);
            check(a.equals(b),"replay differs"); check(f.store.certificationCount()==1,"duplicate certification");
        });
        run("same key changed complaint conflicts", () -> {
            Fixture f=fixture(); String k=key(); f.service.execute(command(1042,VerificationCommand.Decision.VERIFY,null,k,2));
            expect("IDEMPOTENCY_KEY_REUSED",()->f.service.execute(command(1043,VerificationCommand.Decision.VERIFY,null,k,2)));
        });
        run("same key changed rejection reason conflicts", () -> {
            Fixture f=fixture(); String k=key(); f.service.execute(command(1042,VerificationCommand.Decision.REJECT,"First rejection reason",k,2));
            expect("IDEMPOTENCY_KEY_REUSED",()->f.service.execute(command(1042,VerificationCommand.Decision.REJECT,"Another rejection reason",k,2)));
        });
        run("stale-state decision conflicts", () -> {
            Fixture f=fixture(); f.service.execute(command(1042,VerificationCommand.Decision.VERIFY,null,key(),2));
            expect("INVALID_STATE",()->f.service.execute(command(1042,VerificationCommand.Decision.VERIFY,null,key(),2)));
        });
        run("unknown complaint rejected", () -> {
            Fixture f=fixture(); expect("COMPLAINT_NOT_FOUND",()->f.service.execute(command(9999,VerificationCommand.Decision.VERIFY,null,key(),2)));
        });
        run("invalid UUID rejected", () -> {
            Fixture f=fixture(); expect("VALIDATION_FAILED",()->f.service.execute(command(1042,VerificationCommand.Decision.VERIFY,null,"bad-key",2)));
        });
        run("reason not accepted for verify", () -> {
            Fixture f=fixture(); expect("VALIDATION_FAILED",()->f.service.execute(command(1042,VerificationCommand.Decision.VERIFY,"Should not be here",key(),2)));
        });
        run("persistence failure rolls back complaint and certification", () -> {
            Fixture f=fixture(); f.store.simulateComplaintWriteFailureOnce();
            String k=key(); expect("PERSISTENCE_UNAVAILABLE",()->f.service.execute(command(1042,VerificationCommand.Decision.VERIFY,null,k,2)));
            check(status(f,1042)==MaintenanceComplaint.Status.REPORTED,"not rolled back");
            check(f.store.certificationCount()==0,"orphan certification");
            check(f.store.replayCount()==0,"orphan idempotency result");
            f.service.execute(command(1042,VerificationCommand.Decision.VERIFY,null,k,2));
            check(f.store.certificationCount()==1,"retry failed");
        });
        run("two simultaneous decisions commit only once", () -> {
            Fixture f=fixture(); CountDownLatch gate=new CountDownLatch(1);
            AtomicInteger success=new AtomicInteger(), conflict=new AtomicInteger();
            Runnable task=()-> { try { gate.await(); f.service.execute(command(1042,VerificationCommand.Decision.VERIFY,null,key(),2)); success.incrementAndGet(); }
                catch (Exception ex) { if (ex.getMessage()!=null && ex.getMessage().contains("INVALID_STATE")) conflict.incrementAndGet(); else throw new RuntimeException(ex); } };
            Thread a=new Thread(task), b=new Thread(task); a.start(); b.start(); gate.countDown();
            try { a.join(); b.join(); } catch(InterruptedException ex) { Thread.currentThread().interrupt(); throw new RuntimeException(ex); }
            check(success.get()==1 && conflict.get()==1,"concurrency: successes="+success+" conflicts="+conflict);
            check(f.store.certificationCount()==1,"duplicate certification on concurrent attempt");
        });
        System.out.println("TOTAL " + passed + " tests passed, 0 failed");
    }
}
