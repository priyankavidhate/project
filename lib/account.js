'use strict';

let get = (() => {
  var _ref = _asyncToGenerator(function* (id, db) {

    try {

      let response = yield db.get(id);
      return response;
    } catch (e) {

      debug('error in account get :%o', e.message);
      throw e;
    }
  });

  return function get(_x, _x2) {
    return _ref.apply(this, arguments);
  };
})();

let create = (() => {
  var _ref2 = _asyncToGenerator(function* (oldAccount, isNewAccount, db, newAccount) {
    try {

      let account;
      let response;

      debug('oldAccount :%o', oldAccount);
      debug('newAccount :%o', newAccount);

      if (isNewAccount) {

        debug('creeating new account');

        account = oldAccount;
        account['created'] = new Date().toISOString();
        account['confirm'] = JSON.parse(oldAccount.confirm);
        response = yield db.post({ value: account });
      } else {

        debug('updating existing account');

        account = _underscore2.default.extend(oldAccount, newAccount);
        account['created'] = new Date().toISOString();
        account['confirm'] = JSON.parse(account.confirm);
        response = yield db.post({ value: account });
        return response;
      }

      return response;
    } catch (e) {

      debug('error in account create :%o', e.message);
      throw e;
    }
  });

  return function create(_x3, _x4, _x5, _x6) {
    return _ref2.apply(this, arguments);
  };
})();

let isExist = (() => {
  var _ref3 = _asyncToGenerator(function* (phone_number, db) {

    try {

      let viewName = phoneNumberView;
      let options = { keys: [phone_number], include_docs: true, group: false, reduce: false };

      let account = yield db.query(viewName, options);
      debug('account retrived :%o', account);

      return account.rows;
    } catch (e) {

      if (e.message == 'missing') {
        return false;
      }

      debug('error in account isExist :%o', e.message);
      throw e;
    }
  });

  return function isExist(_x7, _x8) {
    return _ref3.apply(this, arguments);
  };
})();

let lastActive = (() => {
  var _ref4 = _asyncToGenerator(function* (account, db) {

    try {

      debug('last activity for :%o', account.doc.value.phone_number);
      let activity = yield db.get(account.doc.value.phone_number);
      debug('last activity :%o', activity);

      return activity.last_active;
    } catch (e) {

      debug('error in account lastActive :%o', e.message);
      throw e;
    }
  });

  return function lastActive(_x9, _x10) {
    return _ref4.apply(this, arguments);
  };
})();

let orgs = (() => {
  var _ref5 = _asyncToGenerator(function* (id, db) {

    try {

      debug('orgs for :%o', id);

      let response = yield db.get(id);

      if (!response.value.orgs) {

        response.value.orgs = [];
      }

      debug('orgs associated with account :%o', response.value.orgs);

      let result = [];
      for (let i = 0; i < response.value.orgs.length; i++) {
        if (response.value.orgs[i].id) {
          result.push(response.value.orgs[i]);
        }
      }

      return result;
    } catch (e) {

      debug('error in account orgs :%o', e.message);
      throw e;
    }
  });

  return function orgs(_x11, _x12) {
    return _ref5.apply(this, arguments);
  };
})();

let del = (() => {
  var _ref6 = _asyncToGenerator(function* (account, db) {

    try {

      let response = yield db.remove(account.doc._id, account.doc._rev);
      debug('response from account delete :%o', response);
      return response;
    } catch (e) {

      debug('error in account delete :%o', e.message);
      throw e;
    }
  });

  return function del(_x13, _x14) {
    return _ref6.apply(this, arguments);
  };
})();

let update = (() => {
  var _ref7 = _asyncToGenerator(function* (account, db) {

    try {

      debug('updated account :%o', account);
      let response = yield db.get(account._id);
      account._rev = response._rev;
      response = yield db.put(account);
      debug('response from account update :%o', response);
      return response;
    } catch (e) {

      if (e.message == 'missing') {

        return 'Account does not exist';
      }

      debug('error in account update :%o', e.message);
      throw e;
    }
  });

  return function update(_x15, _x16) {
    return _ref7.apply(this, arguments);
  };
})();

let getAllAccounts = (() => {
  var _ref8 = _asyncToGenerator(function* (keys, db) {

    try {
      if (!db) {
        db = accountDb;
      }

      let viewName = orgView;

      debug('GetAllAccounts keys :%o', keys);
      let options = { keys: keys, include_docs: false, group: false, reduce: false };

      let account = yield db.query(viewName, options);

      let accounts = [];
      if (!account.rows) {
        throw new Error('Accounts does not have rows');
      }
      for (let i = 0; i < account.rows.length; i++) {
        if (!account.rows[i].id) {
          continue;
        }
        accounts.push(account.rows[i].id);
      }
      debug('Account retrived :%o', accounts);

      return _underscore2.default.pluck(account.rows, 'value');
    } catch (e) {

      debug('Error in getAllAccounts :%o', e.message);
      throw e;
    }
  });

  return function getAllAccounts(_x17, _x18) {
    return _ref8.apply(this, arguments);
  };
})();

let getAllAccountsByOrgs = (() => {
  var _ref9 = _asyncToGenerator(function* (req, res) {

    try {

      let viewName = orgView;

      debug('getAllAccountsByOrgs keys :%o', req.body.keys);
      let keys = [];

      for (let i = 0; i < req.body.keys.length; i++) {
        keys.push(_underscore2.default.values(req.body.keys[i]));
      }

      debug("keys :%o", keys);
      let options = { keys: keys, include_docs: true, group: false, reduce: false };

      let account = yield accountDb.query(viewName, options);

      let accounts = [];
      if (!account.rows) {
        throw new Error('Accounts does not have rows');
      }
      debug(account);
      for (let i = 0; i < account.rows.length; i++) {
        debug(account.rows[i].doc);
        if (!account.rows[i].doc.value.phone_number && !account.rows[i].doc.value.name && !account.rows[i].doc.value.confirm) {
          continue;
        }
        let obj = {};
        obj.name = account.rows[i].doc.value.name;
        obj.phone_number = account.rows[i].doc.value.phone_number;
        obj.profile_pic = account.rows[i].doc.value.profile_pic;
        accounts.push(obj);
      }
      debug('Account retrived :%o', accounts);

      return res.status(200).send(accounts);
    } catch (e) {

      debug('Error in getAllAccountsByOrgs :%o', e.message);
      return res.status(500).send();
    }
  });

  return function getAllAccountsByOrgs(_x19, _x20) {
    return _ref9.apply(this, arguments);
  };
})();

let recordActivity = (() => {
  var _ref10 = _asyncToGenerator(function* (req, res) {

    let doc = {};

    try {

      if (req.account && req.account.value && req.account.value.phone_number) {

        doc._id = req.account.value.phone_number;
        doc.last_active = Date.now();

        debug("Activity doc :%o", doc);

        let response = yield activityDb.get(doc._id);
        doc._rev = response._rev;
        response = yield activityDb.put(doc);

        if (response.ok != true) {
          throw new Error('error while putting into db');
        }

        return res.status(201).send('');
      }

      return res.status(400).send('');
    } catch (e) {

      if (e.message == 'missing') {

        try {

          debug('missing docuemnt, creating new doc with body :%o', doc);
          let response = yield activityDb.put(doc);
          debug('response from adding missing doc :%o', response);

          if (response.ok != true) {
            throw new Error('error while putting into db');
          }

          return res.status(201).send('');
        } catch (e) {
          throw e;
        }
      }

      debug('error in recordActivity:%o', e.message);
      return res.status(500).send('');
    }
  });

  return function recordActivity(_x21, _x22) {
    return _ref10.apply(this, arguments);
  };
})();

let feedback = (() => {
  var _ref11 = _asyncToGenerator(function* (req, res) {

    let doc = {};

    try {

      debug('req.body :', req.body);
      debug('req.account :', req.account);

      if (req.body.message && req.account && req.account._id && req.account.value && req.account.value.phone_number) {

        doc.message = req.body.message;
        doc.phone_number = req.account.value.phone_number;
        doc.account_id = req.account._id;

        yield feedbackDb.post(doc);

        return res.status(201).send('');
      }

      return res.status(400).send('');
    } catch (e) {

      debug('error in recordActivity:%o', e.message);
      return res.status(500).send('');
    }
  });

  return function feedback(_x23, _x24) {
    return _ref11.apply(this, arguments);
  };
})();

var _dotenv = require('dotenv');

var _dotenv2 = _interopRequireDefault(_dotenv);

var _pouchdb = require('pouchdb');

var _pouchdb2 = _interopRequireDefault(_pouchdb);

var _abacusDebug = require('abacus-debug');

var _abacusDebug2 = _interopRequireDefault(_abacusDebug);

var _underscore = require('underscore');

var _underscore2 = _interopRequireDefault(_underscore);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _asyncToGenerator(fn) { return function () { var gen = fn.apply(this, arguments); return new Promise(function (resolve, reject) { function step(key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { return Promise.resolve(value).then(function (value) { step("next", value); }, function (err) { step("throw", err); }); } } return step("next"); }); }; }

const env = _dotenv2.default.config();

const debug = (0, _abacusDebug2.default)('project-account');
const phoneNumberView = 'accountDetails/byPhoneNumber';
const orgView = 'accountDetails/byOrgs';
const activityDbName = process.env.ACTIVITYDB || 'activity';
const feedbackDbName = process.env.FEEDBACKDB || 'feedback';
const accountDbName = process.env.ACCOUNTDB || 'account';
const dburl = process.env.DBURL || '';

let accountDb, activityDb, feedbackDb;
if (dburl === '') {
  debug('empty dburl, creating inmemory db');
  accountDb = new _pouchdb2.default(dburl + accountDbName, { db: require('memdown') });
  activityDb = new _pouchdb2.default(dburl + activityDbName, { db: require('memdown') });
  feedbackDb = new _pouchdb2.default(dburl + feedbackDbName, { db: require('memdown') });
} else {
  accountDb = new _pouchdb2.default(dburl + '/' + accountDbName);
  activityDb = new _pouchdb2.default(dburl + '/' + activityDbName);
  feedbackDb = new _pouchdb2.default(dburl + '/' + feedbackDbName);
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