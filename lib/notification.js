'use strict';

let send = (() => {
  var _ref = _asyncToGenerator(function* (accounts, data, body, ignoreId) {

    try {

      let keys = [];

      for (let i = 0; i < accounts.length; i++) {

        if (accounts[i] != ignoreId) keys.push(accounts[i]);
      }

      debug('keys: %o', keys);

      let response = yield db.allDocs({ include_docs: true, keys: keys });

      if (response.rows) {

        let ids = [];

        for (let i = 0; i < response.rows.length; i++) {

          if (response.rows[i].doc && response.rows[i].doc.registration_id) {
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

        for (let i = 0; i < ids.length; i += 999) {

          // send the message
          gcmObject.send(message, { registrationTokens: ids }, function (err, response) {

            if (err) {
              debug('Error in gcmObject.send :%o', err);
            } else {
              if (response.success) debug('Success response from gcmObject.send :%o', response.success);
              if (response.failure) debug('Failure response from gcmObject.send :%o', response.failure);
            }

            return;
          });
        }
      }
    } catch (e) {
      debug('Error in notification send :%o', e.message);
      throw e;
    }
  });

  return function send(_x, _x2, _x3, _x4) {
    return _ref.apply(this, arguments);
  };
})();

let updateToken = (() => {
  var _ref2 = _asyncToGenerator(function* (req, res) {

    try {

      if (!req.body._id || !req.body.registration_id) {
        return res.status(400).send('Missing parameter.');
      }

      let response = yield db.get(req.body._id);
      req.body._rev = response._rev;
      response = yield db.put(req.body);

      if (response.ok != true) {
        throw new Error('error while putting into db');
      }

      debug('updated refresh token :%o', req.body);
      return res.status(201).send('');
    } catch (e) {

      if (e.message == 'missing') {

        try {

          debug('missing docuemnt, creating new doc with body :%o', req.body);
          let response = yield db.put(req.body);
          debug('response from adding missing doc :%o', response);

          if (response.ok != true) {
            throw new Error('error while putting into db');
          }

          return res.status(201).send('');
        } catch (e) {
          throw e;
        }
      }

      debug('Error in updateToken :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function updateToken(_x5, _x6) {
    return _ref2.apply(this, arguments);
  };
})();

let inviteEmployee = (() => {
  var _ref3 = _asyncToGenerator(function* (phnNumber, orgName, personName) {

    try {

      debug('Adding employee :%o', phnNumber, orgName, personName);

      let result = yield client.messages.create({
        body: 'You have been added to ' + orgName + ' by ' + personName,
        to: phnNumber,
        from: phoneNumber });

      debug('result from message sent :%o', result.status);

      return result;
    } catch (e) {

      if (e.status && e.message) {
        debug('Error in inviteEmployee status %o and message %o', e.status, e.message);
        return e;
      }

      debug('Error in inviteEmployee :%o', e.message);
      throw e;
    }
  });

  return function inviteEmployee(_x7, _x8, _x9) {
    return _ref3.apply(this, arguments);
  };
})();

var _dotenv = require('dotenv');

var _dotenv2 = _interopRequireDefault(_dotenv);

var _underscore = require('underscore');

var _underscore2 = _interopRequireDefault(_underscore);

var _abacusDebug = require('abacus-debug');

var _abacusDebug2 = _interopRequireDefault(_abacusDebug);

var _pouchdb = require('pouchdb');

var _pouchdb2 = _interopRequireDefault(_pouchdb);

var _twilio = require('twilio');

var _twilio2 = _interopRequireDefault(_twilio);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _asyncToGenerator(fn) { return function () { var gen = fn.apply(this, arguments); return new Promise(function (resolve, reject) { function step(key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { return Promise.resolve(value).then(function (value) { step("next", value); }, function (err) { step("throw", err); }); } } return step("next"); }); }; }

const env = _dotenv2.default.config();

const gcm = require('node-gcm');

const api_key = process.env.GCM_API_KEY;
const notificationDbName = process.env.NOTIFICATIONDB || 'notification';
const gcmObject = new gcm.Sender(api_key);
const debug = (0, _abacusDebug2.default)('project-notification');
const dburl = process.env.DBURL || '';

const accountSid = process.env.TWILIO_ACCOUNT_SID;
const authToken = process.env.TWILIO_AUTH_TOKEN;
const phoneNumber = process.env.TWILIO_PHONE_NUMBER;

debug('accountSid :%o', accountSid);
debug('authToken :%o', authToken);
debug('phoneNumber :%o', phoneNumber);

const client = new _twilio2.default(accountSid, authToken);

debug('dbUrl from notification:%o', dburl);

let db;
if (dburl === '') {
  debug('empty dburl, creating inmemory db');
  db = new _pouchdb2.default(dburl + accountDbName, { db: require('memdown') });
} else {
  db = new _pouchdb2.default(dburl + '/' + notificationDbName);
}

module.exports.updateToken = updateToken;
module.exports.send = send;
module.exports.inviteEmployee = inviteEmployee;