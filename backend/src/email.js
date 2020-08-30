'use strict'

import sendgrid from 'sendgrid';
import d from 'abacus-debug';

const debug = d('project-email');
let helper = sendgrid.mail;


async function sendAccount(from, to, org, name, type) {

  const from_email = new helper.Email(from);
  const to_email = new helper.Email(to);
  const subject = 'Welcome to the Project';

  let body;

  if(type == 'org') {

    body = 'Thank you ' + name + ' for creating organization ' + org ;

  }
  else if(type == 'add') {

    body = 'You have been added to the organization ' + org ;

  }

  debug('body:%o', body);

  const content = new helper.Content('text/plain', body);
  const mail = new helper.Mail(from_email, subject, to_email, content);

  await send(mail);

}

async function confirmAccount(from, to, org, name) {

  const from_email = new helper.Email(from);
  const to_email = new helper.Email(to);
  const subject = 'Account confirmation';

  let body = 'Thank you ' + name + ' for confirming your account with organization ' + org ;


  debug('body:%o', body);

  const content = new helper.Content('text/plain', body);
  const mail = new helper.Mail(from_email, subject, to_email, content);

  await send(mail);

}

async function send(mail) {

  let sg = sendgrid(process.env.SENDGRID_API_KEY);
  let request = sg.emptyRequest({
    method: 'POST',
    path: '/v3/mail/send',
    body: mail.toJSON(),
  });

  sg.API(request, function(error, response) {
    if(error) {
      debug('error sending an email : %o', error);
    }
    debug('statusCode : %o', response.statusCode);
    debug('body : %o', response.body);
    debug('headers : %o', response.headers);
    return;

  });
}

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

module.exports.sendAccount = sendAccount;
module.exports.confirmAccount = confirmAccount;

