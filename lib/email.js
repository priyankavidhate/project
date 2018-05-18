'use strict';

let sendAccount = (() => {
  var _ref = _asyncToGenerator(function* (from, to, org, name, type) {

    const from_email = new helper.Email(from);
    const to_email = new helper.Email(to);
    const subject = 'Welcome to the Project';

    let body;

    if (type == 'org') {

      body = 'Thank you ' + name + ' for creating organization ' + org;
    } else if (type == 'add') {

      body = 'You have been added to the organization ' + org;
    }

    debug('body:%o', body);

    const content = new helper.Content('text/plain', body);
    const mail = new helper.Mail(from_email, subject, to_email, content);

    yield send(mail);
  });

  return function sendAccount(_x, _x2, _x3, _x4, _x5) {
    return _ref.apply(this, arguments);
  };
})();

let confirmAccount = (() => {
  var _ref2 = _asyncToGenerator(function* (from, to, org, name) {

    const from_email = new helper.Email(from);
    const to_email = new helper.Email(to);
    const subject = 'Account confirmation';

    let body = 'Thank you ' + name + ' for confirming your account with organization ' + org;

    debug('body:%o', body);

    const content = new helper.Content('text/plain', body);
    const mail = new helper.Mail(from_email, subject, to_email, content);

    yield send(mail);
  });

  return function confirmAccount(_x6, _x7, _x8, _x9) {
    return _ref2.apply(this, arguments);
  };
})();

let send = (() => {
  var _ref3 = _asyncToGenerator(function* (mail) {

    let sg = (0, _sendgrid2.default)(process.env.SENDGRID_API_KEY);
    let request = sg.emptyRequest({
      method: 'POST',
      path: '/v3/mail/send',
      body: mail.toJSON()
    });

    sg.API(request, function (error, response) {
      if (error) {
        debug('error sending an email : %o', error);
      }
      debug('statusCode : %o', response.statusCode);
      debug('body : %o', response.body);
      debug('headers : %o', response.headers);
      return;
    });
  });

  return function send(_x10) {
    return _ref3.apply(this, arguments);
  };
})();

// async function sendOrder(from, to, org, type, status, id) {

//   const from_email = new helper.Email(from);
//   const to_email = new helper.Email(to);
//   const subject = 'welcome to the Project';

//  if(type == 'new order') {
//     body = 'Organization ' + org + ' has placed new order for you';
//   }

//   else if(type == 'status') {
//     body = 'Status for Order ' + id ' has changed. Please open app for more details';
//   }

//   let content = new helper.Content('text/plain', body);
//   let mail = new helper.Mail(from_email, subject, to_email, content);

//   let sg = sendgrid(process.env.SENDGRID_API_KEY);
//   let request = sg.emptyRequest({
//     method: 'POST',
//     path: '/v3/mail/send',
//     body: mail.toJSON(),
//   });

//   sg.API(request, function(error, response) {
//     debug('statusCode : %o', response.statusCode);
//     debug('body : %o', response.body);
//     debug('headers : %o', response.headers);
//   });

//   if(type == 'new order') {

//     body = 'You placed an order ' + org + ' has placed new order for you';

//     content = new helper.Content('text/plain', body);
//     mail = new helper.Mail(from_email, subject, to_email, content);

//     sg = sendgrid(process.env.SENDGRID_API_KEY);
//     request = sg.emptyRequest({
//       method: 'POST',
//       path: '/v3/mail/send',
//       body: mail.toJSON(),
//     });

//     sg.API(request, function(error, response) {
//       debug('statusCode : %o', response.statusCode);
//       debug('body : %o', response.body);
//       debug('headers : %o', response.headers);
//     });

//    }

var _sendgrid = require('sendgrid');

var _sendgrid2 = _interopRequireDefault(_sendgrid);

var _abacusDebug = require('abacus-debug');

var _abacusDebug2 = _interopRequireDefault(_abacusDebug);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _asyncToGenerator(fn) { return function () { var gen = fn.apply(this, arguments); return new Promise(function (resolve, reject) { function step(key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { return Promise.resolve(value).then(function (value) { step("next", value); }, function (err) { step("throw", err); }); } } return step("next"); }); }; }

const debug = (0, _abacusDebug2.default)('project-email');
let helper = _sendgrid2.default.mail;

module.exports.sendAccount = sendAccount;
module.exports.confirmAccount = confirmAccount;