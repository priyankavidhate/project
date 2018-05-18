'use strict'

import dotenv from 'dotenv'
const env = dotenv.config();

import pouchdb from 'pouchdb';
import d from 'abacus-debug';
import _ from 'underscore';

const orgDbName = process.env.ORGDB || 'org';
const dburl = process.env.DBURL || '';

const tagView = 'orgDetails/byTag';
const nameView = 'orgDetails/byName';

const debug = d('project-org');

let orgDb;
if(dburl === '') {
  debug('empty dburl, creating inmemory db');
  orgDb = new pouchdb(dburl+'/'+orgDbName, {db : require('memdown')});
}
else{
  orgDb = new pouchdb(dburl+'/'+orgDbName);
}

async function get(id) {

  try{

    let response = await orgDb.get(id);
    return response;

  }
  catch(e) {

    debug('error in org get :%o', e.message);
    throw e;

  }

}

async function create(org, db) {

  try{

    let response = await db.post(org);
    return response

  }
  catch(e) {

    debug('error in org create :%o', e.message);
    throw e;

  }

}


async function del(org, db) {

  try{

    let response = await db.remove(org.doc._id, org.doc._rev);
    debug('response from org delete :%o', response);
    return response;

  }
  catch(e) {

    debug('error in org delete :%o', e.message);
    throw e;

  }

}


async function search(req, res) {

  try{

    let id = req.params.id;
    let options = {startkey:id, endkey:id+'zzzzzz', include_docs: true, group: false, reduce: false, limit: 50}

    debug('type: %o, id: %o',req.query.type, req.params.id)

    if(req.query.type && req.query.type == 'tag') {

      let response = await orgDb.query(tagView,options);

      if(response.rows) {

        let value = [];

        for(let i =0; i< response.rows.length; i++){
          if(response.rows[i].doc && response.rows[i].doc.value){
            let d = response.rows[i].doc.value;
            d.id = response.rows[i].doc._id;
            value.push(d);
          }
        }

        debug('orgs :%o', value);
        return res.status(200).send(value)
      }
      else{
       return res.status(200).send([]);
      }

    }
    else if(req.query.type && req.query.type == 'name') {

      let response = await orgDb.query(nameView,options);
      if(response.rows) {
        let value = [];

        for(let i =0; i< response.rows.length; i++){
          if(response.rows[i].doc && response.rows[i].doc.value){
            let d = response.rows[i].doc.value;
            d.id = response.rows[i].doc._id;
            value.push(response.rows[i].doc.value);
          }
        }

        debug('orgs :%o', value);

        return res.status(200).send(value)
      }
      else{
       return res.status(200).send([]);
      }

    }
    else{
      res.status(422).send('');
    }

  }
  catch(e) {

    if(e.message == 'missing') {
      debug('missing error')
      return res.status(200).send([]);
    }

    debug('error in search :%o', e.message);
    return res.status(500).send('');

  }

}

async function updatePair(firstOrg, secondOrg, status) {

  try{

    debug('id :%o, orgId :%o', firstOrg.id, secondOrg.id);

    let response = await orgDb.get(firstOrg.id);
    if(!response.value.pair){
      response.value.pair = [];
    }

    for(let i = 0; i < response.value.pair.length; i++){
      if(response.value.pair[i].id == secondOrg.id){

        debug('org already exist in updatePair :%o, %o', secondOrg.id, status);
        if(status == 'accept' || status == 'ignore'){
          response.value.pair[i].status = status;
          break;
        }
        return {message : 'already exist'};
      }
    }

    if( status != 'accept' && status != 'ignore'){
      let org = {};
      if(secondOrg.id && secondOrg.tag && secondOrg.name){
        org.id = secondOrg.id;
        org.tag = secondOrg.tag;
        org.name = secondOrg.name;
        org.status = status;
        response.value.pair.push(org);
      }
    }

    debug('updatePair put doc: %o', response);

    response = await orgDb.put(response);

    debug('result from updatePair put : %o', response);
    return response;

  }
  catch(e) {

    debug('error in updatePair :%o', e.message);
    throw e;

  }

}


async function pair(firstOrg, secondOrg) {

  try{

    return Promise.all([updatePair(firstOrg, secondOrg, "pending"), updatePair(secondOrg, firstOrg, "request_sent")])
    .then((r) => {

      debug('result from promise.all pair: %o', r);

      if(r.length > 1 && r[0].message && r[0].message == 'already exist' && r[1].message && r[1].message == 'already exist'){
        return '409';
      }

      return r;

    })
    .catch((err) => {
      throw err;
    })

  }
  catch(e) {
    debug('error in pair :%o', e.message);
    throw e;

  }

}

async function getPendingPair(req, res) {

  try{

    let id = req.params.id;

    let response = await orgDb.get(id);

    debug('response get getPendingPair: %o', response);

    let result = [];
    let orgs = [];
    if(!response.value.pair) {
      return res.status(200).send(result);
    }

    for(let i=0; i<response.value.pair.length; i++){
      if(response.value.pair[i].status == 'pending'){
        orgs.push(response.value.pair[i].id);
      }
    }

    let r = await orgDb.allDocs({keys: orgs, include_docs: true});

    for(let i =0; i<r.rows.length; i++){

      if(r.rows[i].doc && r.rows[i].doc.value && r.rows[i].id) {
        delete r.rows[i].doc.value.pair;
        let d = r.rows[i].doc.value;
        d.id = r.rows[i].id;
        result.push(d);
      }
    }

    debug('result getPendingPair :%o', result);

    return res.status(200).send(result);

  }
  catch(e) {
    debug('error in getPendingPair :%o', e.message);
    return res.status(500).send('');

  }

}

async function getRequestedPair(req, res) {

  try{

    let id = req.params.id;

    let response = await orgDb.get(id);

     let result = [];
     let orgs = [];
    if(!response.value || !response.value.pair) {
      return res.status(200).send(result);
    }

    for(let i=0; i<response.value.pair.length; i++){
      if(response.value.pair[i].status == 'request_sent'){
        orgs.push(response.value.pair[i].id);
      }
    }

    if(orgs.length > 0) {

      let r = await orgDb.allDocs({keys: orgs, include_docs: true});

      for(let i =0; i<r.rows.length; i++){

        if(r.rows[i].doc && r.rows[i].doc.value && r.rows[i].id) {
          delete r.rows[i].doc.value.pair;
          let d = r.rows[i].doc.value;
          d.id = r.rows[i].id;
          result.push(d);
        }
      }

    }

    debug('result getRequestedPair :%o', result);


    return res.status(200).send(result);

  }
  catch(e) {
    debug('error in getRequestedPair :%o', e.message);
    return res.status(500).send('');

  }

}

async function performAction(body){

  try{

    if(body.action != 'accept' && body.action != 'ignore'){
      debug("performAction Sending 422 for :%o", body)
      return '422';
    }

    debug('%o org Performing action %o on %o', body.second_org.name, body.action, body.first_org.name);

    return Promise.all([updatePair(body.first_org, body.second_org, body.action), updatePair(body.second_org, body.first_org, body.action)])
    .then((r) => {

      debug('result from promise.all performAction: %o', r);

      return '200';

    })
    .catch((err) => {
      throw err;
    })

  }
  catch(e){

    if(e.message){
      debug('error in org performAction :%o', e.message);
    }
    throw e;

  }

}

async function getPairedOrgs(req, res) {

  try{

    let id = req.params.id;

    let response = await orgDb.get(id);

     let result = [];
     let orgs = [];
    if(!response.value || !response.value.pair) {
      return res.status(200).send(result);
    }

    for(let i=0; i<response.value.pair.length; i++){
      if(response.value.pair[i].status == 'accept'){
        orgs.push(response.value.pair[i].id);
      }
    }

    if(orgs.length > 0) {

      let r = await orgDb.allDocs({keys: orgs, include_docs: true});

      for(let i =0; i<r.rows.length; i++){

        if(r.rows[i].doc && r.rows[i].doc.value && r.rows[i].id) {
          delete r.rows[i].doc.value.pair;
          let d = r.rows[i].doc.value;
          d.id = r.rows[i].id;
          result.push(d);
        }
      }

    }

    debug('result getPairedOrgs :%o', result);


    return res.status(200).send(result);

  }
  catch(e) {
    debug('error in getPairedOrgs :%o', e.message);
    return res.status(500).send('');

  }

}

async function update(org){

  try{

    debug('In update');

    if(!org.id){
      return '422'
    }

    debug('Getting org for id :%o', org.id);

    let response = await orgDb.get(org.id);
    debug(response);

    response.value = _.extend(response.value, org);

    if(response.value.id){
      delete response.value.id;
    }

    let doc = {};
    doc._id = org.id;
    doc._rev = response._rev;
    doc.value = response.value;

    debug('Putting org in db :%o', doc);

    response = await orgDb.put(doc);
    doc.value.id = org.id;
    let pair = [];
    if(doc.value.pair){
      pair = doc.value.pair;
      delete doc.value.pair;

    }
    return {value: doc.value, pair: pair};

  }
  catch(e){

    if(e.message == 'missing'){
      return 'Org does not exist'
    }
    debug('error in update :%o', e.message);
    throw e;
  }

}


module.exports.create = create;
module.exports.get = get;
module.exports.del = del;
module.exports.search = search;
module.exports.pair = pair;
module.exports.getPendingPair = getPendingPair;
module.exports.getRequestedPair = getRequestedPair;
module.exports.performAction = performAction;
module.exports.getPairedOrgs = getPairedOrgs;
module.exports.update = update;

