'use strict'

import d from 'abacus-debug';
const debug = d('project-helper');
const accountPhoneView = 'value/byPhoneNumber';



async function isOrg(org, db) {

  try{

    let id = org;
    debug('org id :%o ', id);

    let viewName = 'value/org-view';
    let options = {keys:[id], group: false, reduce: false}

    let response = await db.query(viewName, options);
    debug('value: %o', response.rows);
    if(response.rows.length > 0) {
      return true;
    }
    return false;

  }
  catch(e) {
    if(e.message == 'missing') {
      return false;
    }

    debug('error while geting org:%o', e.message);
    throw e;
  }

}

async function isEmployeeOfOrg(id, org, db) {

  try{

    debug('org and id :%o, %o ', org, id);

    let viewName = 'value/email-view';
    let options = {keys:[id], include_docs: true, group: false, reduce: false}

    let response = await db.query(viewName, options);
    debug('value: %o', response.rows);

    if(response.rows.length > 0 && response.rows[0].doc.value.org == org) {
      return 'employee exist in same org';
    }
    else if(response.rows.length > 0) {
      return 'employee exist';
    }

    return 'new employee';

  }
  catch(e) {
    if(e.message == 'missing') {
      return false;
    }

    debug('error while geting org:%o', e.message);
    throw e;
  }

}

async function employeeCountForOrg(org, db) {

  try{

    let id = org;
    debug('org id :%o ', id);

    let viewName = 'value/org-view';
    let options = {keys:[id], group: true, reduce: true}

    let response = await db.query(viewName, options);
    debug('value: %o', response.rows[0].doc);
    return response.rows[0].doc.value.value;

  }
  catch(e) {
    if(e.message == 'missing') {
      return 'org does not exist'
    }

    debug('error while geting employee count for org:%o', e.message);
    throw e;
  }

}

async function isAccountExist(phoneNumber, db) {
  try{

    let viewName = accountPhoneView;
    let options = {keys:[phoneNumber], group: false, reduce: false}

    let response = await db.query(viewName, options);
    debug('value: %o', response.rows[0].doc);
    //@TODO check response if phone number is not there
    return response.rows;

  }
  catch(e) {

    debug('error in isAccountExist :%o', e.message);
    throw e;

  }
}

module.exports.isOrg = isOrg;
module.exports.employeeCountForOrg = employeeCountForOrg;
module.exports.isEmployeeOfOrg = isEmployeeOfOrg;
module.exports.isAccountExist = isAccountExist;
