'use strict'

import dotenv from 'dotenv'
const env = dotenv.config();

import pouchdb from 'pouchdb';
import d from 'abacus-debug';
import _ from 'underscore';

const debug = d('project-account');
const phoneNumberView = 'accountDetails/byPhoneNumber';
const orgView = 'accountDetails/byOrgs'
const activityDbName = process.env.ACTIVITYDB || 'activity';
const feedbackDbName = process.env.FEEDBACKDB || 'feedback';
const accountDbName = process.env.ACCOUNTDB || 'account';
const dburl = process.env.DBURL || '';

let accountDb, activityDb, feedbackDb;
if(dburl === '') {
  debug('empty dburl, creating inmemory db');
  accountDb = new pouchdb(dburl+accountDbName, {db : require('memdown')});
  activityDb = new pouchdb(dburl+activityDbName, {db : require('memdown')});
  feedbackDb = new pouchdb(dburl+feedbackDbName, {db : require('memdown')});
}
else{
  accountDb = new pouchdb(dburl+'/'+accountDbName);
  activityDb = new pouchdb(dburl+'/'+activityDbName);
  feedbackDb = new pouchdb(dburl+'/'+feedbackDbName);
}

async function get(id, db) {

  try{

    let response = await db.get(id);
    return response;

  }
  catch(e) {

    debug('error in account get :%o', e.message);
    throw e;

  }

}


async function create(oldAccount, isNewAccount, db, newAccount) {
  try{

    let account;
    let response;

    debug('oldAccount :%o', oldAccount);
    debug('newAccount :%o', newAccount);

    if(isNewAccount) {

      debug('creeating new account');

      account = oldAccount;
      account['created'] = new Date().toISOString();
      account['confirm'] = JSON.parse(oldAccount.confirm);
      response = await db.post({value: account});

    }
    else {

      debug('updating existing account');

      account = _.extend(oldAccount, newAccount)
      account['created'] = new Date().toISOString();
      account['confirm'] = JSON.parse(account.confirm);
      response = await db.post({value: account});
      return response;

    }

    return response;

  }
  catch(e) {

    debug('error in account create :%o', e.message);
    throw e;

  }
}

async function isExist(phone_number, db) {

  try{

    let viewName = phoneNumberView;
    let options = {keys:[phone_number], include_docs: true, group: false, reduce: false}

    let account = await db.query(viewName,options);
    debug('account retrived :%o', account);

    return account.rows;

  }
  catch(e){

    if(e.message == 'missing') {
      return false;
    }

    debug('error in account isExist :%o', e.message);
    throw e;

  }
}


async function lastActive(account, db) {

  try{

    debug('last activity for :%o', account.doc.value.phone_number);
    let activity = await db.get(account.doc.value.phone_number);
    debug('last activity :%o', activity);

    return activity.last_active;

  }
  catch(e) {

    debug('error in account lastActive :%o', e.message);
    throw e;

  }

}

async function orgs(id, db) {

  try{

    debug('orgs for :%o', id);

    let response = await db.get(id);

    if(!response.value.orgs) {

      response.value.orgs = [];

    }

    debug('orgs associated with account :%o', response.value.orgs);

    let result = [];
    for(let i=0; i<response.value.orgs.length; i++){
      if(response.value.orgs[i].id){
        result.push(response.value.orgs[i]);
      }
    }

    return result;

  }
  catch(e) {

    debug('error in account orgs :%o', e.message);
    throw e;

  }

}

async function del(account, db) {

  try{

    let response = await db.remove(account.doc._id, account.doc._rev);
    debug('response from account delete :%o', response);
    return response;

  }
  catch(e) {

    debug('error in account delete :%o', e.message);
    throw e;

  }
}

async function update(account, db) {

  try{

    debug('updated account :%o', account);
    let response = await db.get(account._id);
    account._rev = response._rev;
    response = await db.put(account);
    debug('response from account update :%o', response);
    return response;

  }
  catch(e) {

    if(e.message == 'missing') {

      return 'Account does not exist';
    }


    debug('error in account update :%o', e.message);
    throw e;

  }
}

async function getAllAccounts(keys, db) {

  try{
    if(!db) {
      db = accountDb
    }

    let viewName = orgView;

    debug('GetAllAccounts keys :%o', keys);
    let options = {keys:keys, include_docs: false, group: false, reduce: false}

    let account = await db.query(viewName,options);

    let accounts = [];
    if(!account.rows){
      throw new Error('Accounts does not have rows')
    }
    for(let i=0; i<account.rows.length; i++){
      if(!account.rows[i].id){
        continue;
      }
      accounts.push(account.rows[i].id);
    }
    debug('Account retrived :%o', accounts);

    return _.pluck(account.rows, 'value');

  }
  catch(e){

    debug('Error in getAllAccounts :%o', e.message);
    throw e;

  }
}

async function getAllAccountsByOrgs(req, res) {

  try{

    let viewName = orgView;

    debug('getAllAccountsByOrgs keys :%o', req.body.keys);
    let keys=[];

    for(let i=0; i<req.body.keys.length; i++){
      keys.push(_.values(req.body.keys[i]));
    }

    debug("keys :%o", keys);
    let options = {keys:keys, include_docs: true, group: false, reduce: false}

    let account = await accountDb.query(viewName,options);

    let accounts = [];
    if(!account.rows){
      throw new Error('Accounts does not have rows')
    }
    debug(account);
    for(let i=0; i<account.rows.length; i++){
      debug(account.rows[i].doc);
      if(!account.rows[i].doc.value.phone_number && !account.rows[i].doc.value.name && !account.rows[i].doc.value.confirm){
        continue;
      }
      let obj = {}
      obj.name = account.rows[i].doc.value.name;
      obj.phone_number = account.rows[i].doc.value.phone_number;
      obj.profile_pic = account.rows[i].doc.value.profile_pic;
      accounts.push(obj);
    }
    debug('Account retrived :%o', accounts);

    return res.status(200).send(accounts);

  }
  catch(e){

    debug('Error in getAllAccountsByOrgs :%o', e.message);
    return res.status(500).send();
  }
}

async function recordActivity(req, res){

  let doc = {};

  try{

    if(req.account && req.account.value && req.account.value.phone_number){

      doc._id  = req.account.value.phone_number;
      doc.last_active = Date.now();

      debug("Activity doc :%o", doc);

      let response = await activityDb.get(doc._id);
      doc._rev = response._rev;
      response = await activityDb.put(doc);

      if(response.ok !=  true) {
        throw new Error('error while putting into db');
      }

      return res.status(201).send('');
    }

    return res.status(400).send('');

  }
  catch(e){

    if(e.message == 'missing') {

      try{

        debug('missing docuemnt, creating new doc with body :%o', doc)
        let response = await activityDb.put(doc);
        debug('response from adding missing doc :%o', response);

        if(response.ok !=  true) {
          throw new Error('error while putting into db');
        }

        return res.status(201).send('');
      }
      catch(e){
        throw e;
      }

    }

    debug('error in recordActivity:%o', e.message);
    return res.status(500).send('');

  }

}

async function feedback(req, res){

  let doc = {};

  try{

    debug('req.body :', req.body);
    debug('req.account :', req.account);

     if(req.body.message && req.account && req.account._id && req.account.value && req.account.value.phone_number){

      doc.message = req.body.message;
      doc.phone_number = req.account.value.phone_number;
      doc.account_id = req.account._id;

      await feedbackDb.post(doc);

      return res.status(201).send('')
     }

    return res.status(400).send('');

  }
  catch(e){

    debug('error in recordActivity:%o', e.message);
    return res.status(500).send('');

  }

}



module.exports.create = create;
module.exports.isExist = isExist;
module.exports.del = del;
module.exports.lastActive = lastActive;
module.exports.orgs = orgs;
module.exports.update = update;
module.exports.getAllAccounts = getAllAccounts;
module.exports.getAllAccountsByOrgs = getAllAccountsByOrgs;
module.exports.recordActivity = recordActivity;
module.exports.feedback = feedback;
