'use strict';

let isOrg = (() => {
  var _ref = _asyncToGenerator(function* (org, db) {

    try {

      let id = org;
      debug('org id :%o ', id);

      let viewName = 'value/org-view';
      let options = { keys: [id], group: false, reduce: false };

      let response = yield db.query(viewName, options);
      debug('value: %o', response.rows);
      if (response.rows.length > 0) {
        return true;
      }
      return false;
    } catch (e) {
      if (e.message == 'missing') {
        return false;
      }

      debug('error while geting org:%o', e.message);
      throw e;
    }
  });

  return function isOrg(_x, _x2) {
    return _ref.apply(this, arguments);
  };
})();

let isEmployeeOfOrg = (() => {
  var _ref2 = _asyncToGenerator(function* (id, org, db) {

    try {

      debug('org and id :%o, %o ', org, id);

      let viewName = 'value/email-view';
      let options = { keys: [id], include_docs: true, group: false, reduce: false };

      let response = yield db.query(viewName, options);
      debug('value: %o', response.rows);

      if (response.rows.length > 0 && response.rows[0].doc.value.org == org) {
        return 'employee exist in same org';
      } else if (response.rows.length > 0) {
        return 'employee exist';
      }

      return 'new employee';
    } catch (e) {
      if (e.message == 'missing') {
        return false;
      }

      debug('error while geting org:%o', e.message);
      throw e;
    }
  });

  return function isEmployeeOfOrg(_x3, _x4, _x5) {
    return _ref2.apply(this, arguments);
  };
})();

let employeeCountForOrg = (() => {
  var _ref3 = _asyncToGenerator(function* (org, db) {

    try {

      let id = org;
      debug('org id :%o ', id);

      let viewName = 'value/org-view';
      let options = { keys: [id], group: true, reduce: true };

      let response = yield db.query(viewName, options);
      debug('value: %o', response.rows[0].doc);
      return response.rows[0].doc.value.value;
    } catch (e) {
      if (e.message == 'missing') {
        return 'org does not exist';
      }

      debug('error while geting employee count for org:%o', e.message);
      throw e;
    }
  });

  return function employeeCountForOrg(_x6, _x7) {
    return _ref3.apply(this, arguments);
  };
})();

let isAccountExist = (() => {
  var _ref4 = _asyncToGenerator(function* (phoneNumber, db) {
    try {

      let viewName = accountPhoneView;
      let options = { keys: [phoneNumber], group: false, reduce: false };

      let response = yield db.query(viewName, options);
      debug('value: %o', response.rows[0].doc);
      //@TODO check response if phone number is not there
      return response.rows;
    } catch (e) {

      debug('error in isAccountExist :%o', e.message);
      throw e;
    }
  });

  return function isAccountExist(_x8, _x9) {
    return _ref4.apply(this, arguments);
  };
})();

var _abacusDebug = require('abacus-debug');

var _abacusDebug2 = _interopRequireDefault(_abacusDebug);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _asyncToGenerator(fn) { return function () { var gen = fn.apply(this, arguments); return new Promise(function (resolve, reject) { function step(key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { return Promise.resolve(value).then(function (value) { step("next", value); }, function (err) { step("throw", err); }); } } return step("next"); }); }; }

const debug = (0, _abacusDebug2.default)('project-helper');
const accountPhoneView = 'value/byPhoneNumber';

module.exports.isOrg = isOrg;
module.exports.employeeCountForOrg = employeeCountForOrg;
module.exports.isEmployeeOfOrg = isEmployeeOfOrg;
module.exports.isAccountExist = isAccountExist;