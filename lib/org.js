'use strict';

let get = (() => {
  var _ref = _asyncToGenerator(function* (id) {

    try {

      let response = yield orgDb.get(id);
      return response;
    } catch (e) {

      debug('error in org get :%o', e.message);
      throw e;
    }
  });

  return function get(_x) {
    return _ref.apply(this, arguments);
  };
})();

let create = (() => {
  var _ref2 = _asyncToGenerator(function* (org, db) {

    try {

      let response = yield db.post(org);
      return response;
    } catch (e) {

      debug('error in org create :%o', e.message);
      throw e;
    }
  });

  return function create(_x2, _x3) {
    return _ref2.apply(this, arguments);
  };
})();

let del = (() => {
  var _ref3 = _asyncToGenerator(function* (org, db) {

    try {

      let response = yield db.remove(org.doc._id, org.doc._rev);
      debug('response from org delete :%o', response);
      return response;
    } catch (e) {

      debug('error in org delete :%o', e.message);
      throw e;
    }
  });

  return function del(_x4, _x5) {
    return _ref3.apply(this, arguments);
  };
})();

let search = (() => {
  var _ref4 = _asyncToGenerator(function* (req, res) {

    try {

      let id = req.params.id;
      let options = { startkey: id, endkey: id + 'zzzzzz', include_docs: true, group: false, reduce: false, limit: 50 };

      debug('type: %o, id: %o', req.query.type, req.params.id);

      if (req.query.type && req.query.type == 'tag') {

        let response = yield orgDb.query(tagView, options);

        if (response.rows) {

          let value = [];

          for (let i = 0; i < response.rows.length; i++) {
            if (response.rows[i].doc && response.rows[i].doc.value) {
              let d = response.rows[i].doc.value;
              d.id = response.rows[i].doc._id;
              value.push(d);
            }
          }

          debug('orgs :%o', value);
          return res.status(200).send(value);
        } else {
          return res.status(200).send([]);
        }
      } else if (req.query.type && req.query.type == 'name') {

        let response = yield orgDb.query(nameView, options);
        if (response.rows) {
          let value = [];

          for (let i = 0; i < response.rows.length; i++) {
            if (response.rows[i].doc && response.rows[i].doc.value) {
              let d = response.rows[i].doc.value;
              d.id = response.rows[i].doc._id;
              value.push(response.rows[i].doc.value);
            }
          }

          debug('orgs :%o', value);

          return res.status(200).send(value);
        } else {
          return res.status(200).send([]);
        }
      } else {
        res.status(422).send('');
      }
    } catch (e) {

      if (e.message == 'missing') {
        debug('missing error');
        return res.status(200).send([]);
      }

      debug('error in search :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function search(_x6, _x7) {
    return _ref4.apply(this, arguments);
  };
})();

let updatePair = (() => {
  var _ref5 = _asyncToGenerator(function* (firstOrg, secondOrg, status) {

    try {

      debug('id :%o, orgId :%o', firstOrg.id, secondOrg.id);

      let response = yield orgDb.get(firstOrg.id);
      if (!response.value.pair) {
        response.value.pair = [];
      }

      for (let i = 0; i < response.value.pair.length; i++) {
        if (response.value.pair[i].id == secondOrg.id) {

          debug('org already exist in updatePair :%o, %o', secondOrg.id, status);
          if (status == 'accept' || status == 'ignore') {
            response.value.pair[i].status = status;
            break;
          }
          return { message: 'already exist' };
        }
      }

      if (status != 'accept' && status != 'ignore') {
        let org = {};
        if (secondOrg.id && secondOrg.tag && secondOrg.name) {
          org.id = secondOrg.id;
          org.tag = secondOrg.tag;
          org.name = secondOrg.name;
          org.status = status;
          response.value.pair.push(org);
        }
      }

      debug('updatePair put doc: %o', response);

      response = yield orgDb.put(response);

      debug('result from updatePair put : %o', response);
      return response;
    } catch (e) {

      debug('error in updatePair :%o', e.message);
      throw e;
    }
  });

  return function updatePair(_x8, _x9, _x10) {
    return _ref5.apply(this, arguments);
  };
})();

let pair = (() => {
  var _ref6 = _asyncToGenerator(function* (firstOrg, secondOrg) {

    try {

      return Promise.all([updatePair(firstOrg, secondOrg, "pending"), updatePair(secondOrg, firstOrg, "request_sent")]).then(function (r) {

        debug('result from promise.all pair: %o', r);

        if (r.length > 1 && r[0].message && r[0].message == 'already exist' && r[1].message && r[1].message == 'already exist') {
          return '409';
        }

        return r;
      }).catch(function (err) {
        throw err;
      });
    } catch (e) {
      debug('error in pair :%o', e.message);
      throw e;
    }
  });

  return function pair(_x11, _x12) {
    return _ref6.apply(this, arguments);
  };
})();

let getPendingPair = (() => {
  var _ref7 = _asyncToGenerator(function* (req, res) {

    try {

      let id = req.params.id;

      let response = yield orgDb.get(id);

      debug('response get getPendingPair: %o', response);

      let result = [];
      let orgs = [];
      if (!response.value.pair) {
        return res.status(200).send(result);
      }

      for (let i = 0; i < response.value.pair.length; i++) {
        if (response.value.pair[i].status == 'pending') {
          orgs.push(response.value.pair[i].id);
        }
      }

      let r = yield orgDb.allDocs({ keys: orgs, include_docs: true });

      for (let i = 0; i < r.rows.length; i++) {

        if (r.rows[i].doc && r.rows[i].doc.value && r.rows[i].id) {
          delete r.rows[i].doc.value.pair;
          let d = r.rows[i].doc.value;
          d.id = r.rows[i].id;
          result.push(d);
        }
      }

      debug('result getPendingPair :%o', result);

      return res.status(200).send(result);
    } catch (e) {
      debug('error in getPendingPair :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function getPendingPair(_x13, _x14) {
    return _ref7.apply(this, arguments);
  };
})();

let getRequestedPair = (() => {
  var _ref8 = _asyncToGenerator(function* (req, res) {

    try {

      let id = req.params.id;

      let response = yield orgDb.get(id);

      let result = [];
      let orgs = [];
      if (!response.value || !response.value.pair) {
        return res.status(200).send(result);
      }

      for (let i = 0; i < response.value.pair.length; i++) {
        if (response.value.pair[i].status == 'request_sent') {
          orgs.push(response.value.pair[i].id);
        }
      }

      if (orgs.length > 0) {

        let r = yield orgDb.allDocs({ keys: orgs, include_docs: true });

        for (let i = 0; i < r.rows.length; i++) {

          if (r.rows[i].doc && r.rows[i].doc.value && r.rows[i].id) {
            delete r.rows[i].doc.value.pair;
            let d = r.rows[i].doc.value;
            d.id = r.rows[i].id;
            result.push(d);
          }
        }
      }

      debug('result getRequestedPair :%o', result);

      return res.status(200).send(result);
    } catch (e) {
      debug('error in getRequestedPair :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function getRequestedPair(_x15, _x16) {
    return _ref8.apply(this, arguments);
  };
})();

let performAction = (() => {
  var _ref9 = _asyncToGenerator(function* (body) {

    try {

      if (body.action != 'accept' && body.action != 'ignore') {
        debug("performAction Sending 422 for :%o", body);
        return '422';
      }

      debug('%o org Performing action %o on %o', body.second_org.name, body.action, body.first_org.name);

      return Promise.all([updatePair(body.first_org, body.second_org, body.action), updatePair(body.second_org, body.first_org, body.action)]).then(function (r) {

        debug('result from promise.all performAction: %o', r);

        return '200';
      }).catch(function (err) {
        throw err;
      });
    } catch (e) {

      if (e.message) {
        debug('error in org performAction :%o', e.message);
      }
      throw e;
    }
  });

  return function performAction(_x17) {
    return _ref9.apply(this, arguments);
  };
})();

let getPairedOrgs = (() => {
  var _ref10 = _asyncToGenerator(function* (req, res) {

    try {

      let id = req.params.id;

      let response = yield orgDb.get(id);

      let result = [];
      let orgs = [];
      if (!response.value || !response.value.pair) {
        return res.status(200).send(result);
      }

      for (let i = 0; i < response.value.pair.length; i++) {
        if (response.value.pair[i].status == 'accept') {
          orgs.push(response.value.pair[i].id);
        }
      }

      if (orgs.length > 0) {

        let r = yield orgDb.allDocs({ keys: orgs, include_docs: true });

        for (let i = 0; i < r.rows.length; i++) {

          if (r.rows[i].doc && r.rows[i].doc.value && r.rows[i].id) {
            delete r.rows[i].doc.value.pair;
            let d = r.rows[i].doc.value;
            d.id = r.rows[i].id;
            result.push(d);
          }
        }
      }

      debug('result getPairedOrgs :%o', result);

      return res.status(200).send(result);
    } catch (e) {
      debug('error in getPairedOrgs :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function getPairedOrgs(_x18, _x19) {
    return _ref10.apply(this, arguments);
  };
})();

let update = (() => {
  var _ref11 = _asyncToGenerator(function* (org) {

    try {

      debug('In update');

      if (!org.id) {
        return '422';
      }

      debug('Getting org for id :%o', org.id);

      let response = yield orgDb.get(org.id);
      debug(response);

      response.value = _underscore2.default.extend(response.value, org);

      if (response.value.id) {
        delete response.value.id;
      }

      let doc = {};
      doc._id = org.id;
      doc._rev = response._rev;
      doc.value = response.value;

      debug('Putting org in db :%o', doc);

      response = yield orgDb.put(doc);
      doc.value.id = org.id;
      let pair = [];
      if (doc.value.pair) {
        pair = doc.value.pair;
        delete doc.value.pair;
      }
      return { value: doc.value, pair: pair };
    } catch (e) {

      if (e.message == 'missing') {
        return 'Org does not exist';
      }
      debug('error in update :%o', e.message);
      throw e;
    }
  });

  return function update(_x20) {
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

const orgDbName = process.env.ORGDB || 'org';
const dburl = process.env.DBURL || '';

const tagView = 'orgDetails/byTag';
const nameView = 'orgDetails/byName';

const debug = (0, _abacusDebug2.default)('project-org');

let orgDb;
if (dburl === '') {
  debug('empty dburl, creating inmemory db');
  orgDb = new _pouchdb2.default(dburl + '/' + orgDbName, { db: require('memdown') });
} else {
  orgDb = new _pouchdb2.default(dburl + '/' + orgDbName);
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