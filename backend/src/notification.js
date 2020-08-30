'use strict'
import dotenv from 'dotenv'
const env = dotenv.config();

import _ from 'underscore';
import d from 'abacus-debug';
import pouchdb from 'pouchdb';
import twilio from 'twilio';

const gcm = require('node-gcm');

const api_key = process.env.GCM_API_KEY;
const notificationDbName = process.env.NOTIFICATIONDB || 'notification';
const gcmObject = new gcm.Sender(api_key);
const debug = d('project-notification');
const dburl = process.env.DBURL || '';

const accountSid = process.env.TWILIO_ACCOUNT_SID;
const authToken = process.env.TWILIO_AUTH_TOKEN;
const phoneNumber = process.env.TWILIO_PHONE_NUMBER;

debug('accountSid :%o', accountSid);
debug('authToken :%o', authToken);
debug('phoneNumber :%o', phoneNumber);


const client = new twilio(accountSid, authToken);

debug('dbUrl from notification:%o', dburl);

let db;
if(dburl === '') {
  debug('empty dburl, creating inmemory db');
  db = new pouchdb(dburl+accountDbName, {db : require('memdown')});
}
else{
  db = new pouchdb(dburl+'/'+notificationDbName);
}


async function send(accounts, data, body, ignoreId) {

  try{

    let keys = [];

    for(let i=0; i<accounts.length; i++){

      if(accounts[i] != ignoreId)
        keys.push(accounts[i])

    }

    debug('keys: %o', keys);

    let response = await db.allDocs({include_docs: true, keys: keys});

    if(response.rows) {

      let ids = [];

      for(let i=0; i<response.rows.length; i++){

        if(response.rows[i].doc && response.rows[i].doc.registration_id) {
          ids.push(response.rows[i].doc.registration_id);
        }

      }

      data.notification_body = body;

      let message = new gcm.Message({
          data: data,
          priority: 'high'
      });

      debug('Message :%o', JSON.stringify(message));
      debug('RegistrationIds :%o', ids);

      for(let i=0; i<ids.length; i+=999){

        // send the message
        gcmObject.send(message, { registrationTokens: ids }, function(err, response) {

          if(err){
            debug('Error in gcmObject.send :%o', err);
          }
          else{
            if(response.success)
              debug('Success response from gcmObject.send :%o', response.success);
            if(response.failure)
              debug('Failure response from gcmObject.send :%o', response.failure);
          }

          return;
        });

      }

    }
  }
  catch(e) {
      debug('Error in notification send :%o', e.message);
      throw e;

  }

}

async function updateToken(req, res) {

  try{

    if(!req.body._id || !req.body.registration_id){
      return res.status(400).send('Missing parameter.');
    }

    let response = await db.get(req.body._id);
    req.body._rev = response._rev;
    response = await db.put(req.body);

    if(response.ok !=  true) {
    throw new Error('error while putting into db');
    }

    debug('updated refresh token :%o', req.body);
    return res.status(201).send('');

  }
  catch(e) {

    if(e.message == 'missing') {

      try{

        debug('missing docuemnt, creating new doc with body :%o', req.body)
        let response = await db.put(req.body);
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

    debug('Error in updateToken :%o', e.message);
    return res.status(500).send('');

  }
}

async function inviteEmployee(phnNumber, orgName, personName){

  try{

    debug('Adding employee :%o', phnNumber, orgName, personName);

    let result = await client.messages.create({
      body: 'You have been added to ' + orgName + ' by ' + personName,
      to: phnNumber,
      from: phoneNumber});

    debug('result from message sent :%o', result.status);

   return result;

  }
  catch(e){

    if( e.status && e.message) {
      debug('Error in inviteEmployee status %o and message %o', e.status, e.message);
      return e;
    }

    debug('Error in inviteEmployee :%o', e.message);
    throw e;

  }

}

module.exports.updateToken = updateToken;
module.exports.send = send;
module.exports.inviteEmployee =inviteEmployee;