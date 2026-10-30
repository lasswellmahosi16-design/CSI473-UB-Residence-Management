#!/usr/bin/env python3
"""Real HTTP integration checks using only Python's standard library."""
import json
import os
import subprocess
import time
import urllib.error
import urllib.request
import uuid
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
PORT=8766
BASE=f'http://127.0.0.1:{PORT}'
N=0

def request(method,route,role=None,body=None,key=None):
    headers={}
    if role: headers['Authorization']='Bearer demo-'+role
    data=None if body is None else json.dumps(body).encode('utf-8')
    if body is not None: headers['Content-Type']='application/json'
    if key is not None: headers['Idempotency-Key']=key
    req=urllib.request.Request(BASE+route,data=data,headers=headers,method=method)
    try:
        with urllib.request.urlopen(req,timeout=6) as result:
            return result.status,json.load(result),dict(result.headers.items())
    except urllib.error.HTTPError as e:
        return e.code,json.loads(e.read()),dict(e.headers.items())

def chk(name,expected,actual):
    global N
    assert actual==expected,f'{name}: expected {expected}, got {actual}'
    print('PASS',name)
    N+=1

if __name__=='__main__':
    env={**os.environ,'DORMHUB_PORT':str(PORT)}
    proc=subprocess.Popen(['java','-Ddormhub.webRoot=web','-cp','build/classes','ub.dormhub.webapp.PrototypeServer'],cwd=ROOT,env=env,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)
    try:
        for i in range(50):
            try:
                if request('GET','/api/health')[0]==200:break
            except Exception: time.sleep(.1)
        else: raise RuntimeError('Server failed to start')
        chk('health',200,request('GET','/api/health')[0])
        chk('auth required',401,request('GET','/api/v1/complaints')[0])
        code,student,_=request('GET','/api/v1/complaints','student')
        chk('student sees only own complaints', [1031,1042], sorted(x['id'] for x in student))
        chk('student cannot see other student data',403,request('GET','/api/v1/complaints/1043','student')[0])
        chk('student cannot verify',403,request('POST','/api/v1/complaints/1042/verification','student',{'decision':'VERIFY'},str(uuid.uuid4()))[0])
        chk('reject missing category',400,request('POST','/api/v1/complaints','student',{'category':'','description':'A leak in my bathroom.'})[0])
        chk('reject too short report',400,request('POST','/api/v1/complaints','student',{'category':'Plumbing','description':'Tap'})[0])
        newtext='The water tap drips and makes a "knocking" sound.'
        code,record,_=request('POST','/api/v1/complaints','student',{'category':'Plumbing','description':newtext})
        chk('student submits report',201,code)
        chk('new record retains escaped description',newtext,record['description'])
        chk('new report appears in student list',True,any(x['id']==record['id'] for x in request('GET','/api/v1/complaints','student')[1]))
        chk('officer sees all reported complaints',True,len(request('GET','/api/v1/complaints','officer')[1])>=5)
        key=str(uuid.uuid4()); verify_url='/api/v1/complaints/1042/verification'
        code,verified,_=request('POST',verify_url,'officer',{'decision':'VERIFY'},key)
        chk('verification creates certification',True,code==201 and verified['status']=='VERIFIED' and verified['certification']['certificationId']>0)
        code,again,headers=request('POST',verify_url,'officer',{'decision':'VERIFY'},key)
        chk('identical retry is idempotent',True,code==201 and again==verified and headers.get('Idempotent-replayed','').lower()=='true')
        chk('duplicate verification blocked',409,request('POST',verify_url,'officer',{'decision':'VERIFY'},str(uuid.uuid4()))[0])
        chk('reject requires explanation',400,request('POST','/api/v1/complaints/1043/verification','officer',{'decision':'REJECT','rejectionReason':'Wrong'},str(uuid.uuid4()))[0])
        rejectkey=str(uuid.uuid4());reject_url='/api/v1/complaints/1043/verification'
        code,rejected,_=request('POST',reject_url,'officer',{'decision':'REJECT','rejectionReason':'Fault is not in the reporting room.'},rejectkey)
        chk('reject updates status without certification',True,code==200 and rejected['status']=='REJECTED' and 'certification' not in rejected)
        chk('same key changed request conflict',409,request('POST',reject_url,'officer',{'decision':'VERIFY'},rejectkey)[0])
        chk('student sees verified status', 'VERIFIED',request('GET','/api/v1/complaints/1042','student')[1]['status'])
        chk('missing complaint returns 404',404,request('GET','/api/v1/complaints/9999','officer')[0])
        def decide(_): return request('POST','/api/v1/complaints/1044/verification','officer',{'decision':'VERIFY'},str(uuid.uuid4()))[0]
        with ThreadPoolExecutor(max_workers=2) as pool: responses=sorted(pool.map(decide,range(2)))
        chk('only one concurrent decision succeeds',[201,409],responses)
        chk('reset demo',200,request('POST','/api/demo/reset','officer')[0])
        chk('reset restores example status','REPORTED',request('GET','/api/v1/complaints/1042','student')[1]['status'])
        chk('reset clears user reports',404,request('GET',f"/api/v1/complaints/{record['id']}",'student')[0])
        print(f'TOTAL {N} integration checks passed; 0 failed')
    finally:
        proc.terminate()
        try:proc.wait(timeout=4)
        except subprocess.TimeoutExpired:proc.kill()
