'use strict';

let get = (() => {
  var _ref = _asyncToGenerator(function* (req, res) {
    try {
      let id = req.params.id;
      debug('Looking for org id :%o', id);
      let response = yield orgDb.get(id);

      if (!response.value || !response.value.items) {
        debug('Returning 404 as response is :%o', response);
        return res.status(404).send('');
      }
      debug('Returning 200 as response is :%o', response);
      return res.status(200).send(response.value.items);
    } catch (e) {
      debug('error in get :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function get(_x, _x2) {
    return _ref.apply(this, arguments);
  };
})();

let post = (() => {
  var _ref2 = _asyncToGenerator(function* (req, res) {
    try {
      let id = req.params.id;
      let body = req.body;

      let updateOrg = {};
      updateOrg.id = id;
      updateOrg.items = body;

      let response = yield org.update(updateOrg);

      if (response == '422') {
        return res.status(422).send('');
      }

      if (response == 'Org does not exist') {
        return res.status(404).send('');
      }

      let title = 'Organization itmes are updated';

      if (response.value && response.value.name) {
        title = 'Organization ' + response.value.name + ' itmes are updated';
      }

      let subject = {};
      subject.body = title;

      let data = {};
      data.value = body;
      data.switch = 'org_itmes_update';

      yield sendNotification(subject, data, [1, 2, 3], response.value);
      return res.status(200).send('');
    } catch (e) {
      debug('error in post :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function post(_x3, _x4) {
    return _ref2.apply(this, arguments);
  };
})();

let sendNotification = (() => {
  var _ref3 = _asyncToGenerator(function* (subject, data, bands, org) {
    try {
      let accounts = [];

      if (!bands instanceof Array || !org.id || !org.name) {
        return;
      }

      for (let i = 0; i < bands.length; i++) {
        accounts.push((yield account.getAllAccounts([[org.id, org.name, bands[i]]])));
      }

      accounts = _underscore2.default.flatten(accounts);
      _notification2.default.send(accounts, data, subject);
    } catch (e) {
      debug('error in sendNotification :%o', e.message);
      throw e;
    }
  });

  return function sendNotification(_x5, _x6, _x7, _x8) {
    return _ref3.apply(this, arguments);
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

var _org = require('../org');

var org = _interopRequireWildcard(_org);

var _account = require('../account');

var account = _interopRequireWildcard(_account);

var _notification = require('../notification');

var _notification2 = _interopRequireDefault(_notification);

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _asyncToGenerator(fn) { return function () { var gen = fn.apply(this, arguments); return new Promise(function (resolve, reject) { function step(key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { return Promise.resolve(value).then(function (value) { step("next", value); }, function (err) { step("throw", err); }); } } return step("next"); }); }; }

const env = _dotenv2.default.config();

const orgDbName = process.env.ORGDB || 'org';
const dburl = process.env.DBURL || '';

const debug = (0, _abacusDebug2.default)('project-orgitems');

let orgDb;
if (dburl === '') {
  debug('empty dburl, creating inmemory db');
  orgDb = new _pouchdb2.default(dburl + '/' + orgDbName, { db: require('memdown') });
} else {
  orgDb = new _pouchdb2.default(dburl + '/' + orgDbName);
}

module.exports.get = get;
module.exports.post = post;