'use strict'

import dotenv from 'dotenv'
const env = dotenv.config();

import pouchdb from 'pouchdb';
import d from 'abacus-debug';
import _ from 'underscore';
import * as org from '../org'
import * as account from '../account'
import notification from '../notification'

const orgDbName = process.env.ORGDB || 'org';
const dburl = process.env.DBURL || '';

const debug = d('project-orgitems');

let orgDb;
if(dburl === '') {
  debug('empty dburl, creating inmemory db');
  orgDb = new pouchdb(dburl+'/'+orgDbName, {db : require('memdown')});
}
else{
  orgDb = new pouchdb(dburl+'/'+orgDbName);
}

async function get(req, res) {
  try {
    let id = req.params.id;
    debug('Looking for org id :%o', id);
    let response = await orgDb.get(id);

    if(!response.value || !response.value.items) {
      debug('Returning 404 as response is :%o', response);
      return res.status(404).send('');
    }
    debug('Returning 200 as response is :%o', response);
    return res.status(200).send(response.value.items);
  }
  catch (e) {
    debug('error in get :%o', e.message);
    return res.status(500).send('');
  }
}

async function post(req, res) {
  try {
    let id = req.params.id;
    let body = req.body;

    let updateOrg = {};
    updateOrg.id = id;
    updateOrg.items = body;

    let response = await org.update(updateOrg);

    if(response == '422') {
      return res.status(422).send('')
    }

    if(response == 'Org does not exist') {
      return res.status(404).send('')
    }

    let title = 'Organization itmes are updated';

    if(response.value && response.value.name) {
      title = 'Organization ' + response.value.name + ' itmes are updated';
    }

    let subject = {};
    subject.body = title;

    let data = {};
    data.value = body;
    data.switch = 'org_itmes_update';

    await sendNotification(subject, data, [1,2,3], response.value);
    return res.status(200).send('')
  }
  catch (e) {
    debug('error in post :%o', e.message);
    return res.status(500).send('');
  }
}

async function sendNotification(subject, data, bands, org){
  try{
    let accounts = [];

    if(!bands instanceof Array || !org.id || !org.name){
      return;
    }

    for(let i=0; i<bands.length; i++){
      accounts.push(await account.getAllAccounts([[org.id, org.name, bands[i]]]));
    }

    accounts = _.flatten(accounts);
    notification.send(accounts, data, subject);
  }
  catch(e){
    debug('error in sendNotification :%o', e.message);
    throw e;
  }
}

module.exports.get = get;
module.exports.post = post;