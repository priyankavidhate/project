'use strict';

let onBoard = (() => {
  var _ref = _asyncToGenerator(function* (req, res) {

    try {

      debug('Starting onBoard process for user');

      let input = req.body;

      debug('req body :%o', input);

      let accountResponse = yield _account2.default.isExist(input.phone_number, db);
      let lastActive = Date.now();

      if (accountResponse.length > 0) {

        debug('account exist');

        if (accountResponse[0].doc && accountResponse[0].doc.value && accountResponse[0].doc.value.confirm) {
          lastActive = yield _account2.default.lastActive(accountResponse[0], activityDb);
        }
        yield _account2.default.del(accountResponse[0], db);
      }

      let response;

      if (Date.now() - lastActive > 45 * day) {

        debug('account is not active for more than 45 days');
        response = yield _account2.default.create(input, true, db);
      } else if (!accountResponse || accountResponse.length == 0) {

        debug('account does not exist, creating new account');
        response = yield _account2.default.create(input, true, db);
      } else {

        debug('account exist but active less than 45 days ago');

        response = yield _account2.default.create(accountResponse[0].doc.value, false, db, input);
      }

      debug('sending response :%o', response);
      return res.status(200).send(response);
    } catch (e) {

      debug('error in onBoard :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function onBoard(_x, _x2) {
    return _ref.apply(this, arguments);
  };
})();

let updateAccount = (() => {
  var _ref2 = _asyncToGenerator(function* (req, res) {

    try {

      let body = req.body;

      if (!req.account || !req.account.value || !req.account.value.phone_number || !body.id) {
        return res.status(422).send('');
      }

      if (req.account._id != body.id) {
        return res.status(403).send('');
      }

      req.account.value = _underscore2.default.extend(req.account.value, body);

      if (req.account.value.id) {
        delete req.account.value.id;
      }

      let response = yield _account2.default.update(req.account, db);
      if (response == 'Account does not exist') {
        return res.status(404).send('');
      }

      return res.status(200).send(response);
    } catch (e) {
      debug('error in updateAccount :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function updateAccount(_x3, _x4) {
    return _ref2.apply(this, arguments);
  };
})();

let getOrgs = (() => {
  var _ref3 = _asyncToGenerator(function* (req, res) {

    try {

      debug('getting orgs');
      if (req.headers.authorization && req.headers.authorization == req.params.id) {

        let accountOrgs = yield _account2.default.orgs(req.params.id, db);
        debug('getting org details for :%o', accountOrgs);

        if (accountOrgs instanceof Array) {

          let orgs = [];

          for (let i = 0; i < accountOrgs.length; i++) {
            orgs.push(accountOrgs[i].id);
          }
          let response = yield orgDb.allDocs({ include_docs: true, keys: orgs });

          let result = [];
          for (let i = 0; i < response.rows.length; i++) {

            if (response.rows[i].doc && response.rows[i].doc._id && response.rows[i].doc.value) {
              let obj = {};
              obj = response.rows[i].doc.value;
              obj.id = response.rows[i].doc._id;
              obj.band = accountOrgs[i].band;
              delete obj.pair;
              result.push(obj);
            }
          }
          debug('sending orgs :%o', result);
          return res.status(200).send(result);
        } else {
          return res.status(401).send('');
        }
      } else {
        return res.status(400).send('');
      }
    } catch (e) {

      debug('error in getOrgs :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function getOrgs(_x5, _x6) {
    return _ref3.apply(this, arguments);
  };
})();

let createOrg = (() => {
  var _ref4 = _asyncToGenerator(function* (req, res) {

    try {

      debug('creating orgs');

      let orgDoc = {};
      orgDoc['value'] = req.body;

      let result = yield _org2.default.create(orgDoc, orgDb);

      orgDoc = {};
      orgDoc['id'] = result.id;
      orgDoc['band'] = 1;
      orgDoc['name'] = req.body.name;

      debug(req.account);

      if (!req.account.value.orgs) {
        req.account.value.orgs = [];
      }

      req.account.value.orgs.push(orgDoc);
      yield _account2.default.update(req.account, db);
      return res.status(200).send(result);
    } catch (e) {

      debug('error in createOrg :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function createOrg(_x7, _x8) {
    return _ref4.apply(this, arguments);
  };
})();

let getInbox = (() => {
  var _ref5 = _asyncToGenerator(function* (req, res) {

    try {
      let id = req.params.id;
      debug('Inbox id :%o ', id);

      let skip = 0;

      if (req.query.skip) {
        skip = req.query.skip;
      }

      let viewName = 'ToFrom/to-view';
      let options = { key: id, include_docs: true, reduce: false, group: false, limit: 1000, skip: skip };

      let response = yield orderDb.query(viewName, options);
      debug('No of orders found: %o', response.rows.length);

      if (response.rows.length < 1) {

        return res.status(404).send({ code: 404, status: 'No order has been made for this account' });
      }

      let result = response.rows;
      let orgs = [];
      let orgsToName = {};

      for (let i = 0; i < response.rows.length; i++) {

        if (response.rows[i].doc.value.from) {
          orgs.push(response.rows[i].doc.value.from);
        }
      }

      debug('before orgs: %o', orgs);
      orgs = _underscore2.default.uniq(orgs);
      debug('after orgs: %o', orgs);
      options = { keys: orgs, include_docs: true };
      response = yield orgDb.allDocs(options);

      debug('response.rows :%o', response.rows);

      for (let i = 0; i < response.rows.length; i++) {

        debug('org tag :%o', response.rows[i].doc.value.tag);

        if (response.rows[i].doc.value.tag) {
          orgsToName[response.rows[i].id] = response.rows[i].doc.value.tag;
        }
      }

      debug(orgsToName);

      for (let i = 0; i < result.length; i++) {

        if (result[i].doc.value.from) {
          result[i].doc.value.from = orgsToName[result[i].doc.value.from];
        }
      }

      return res.status(200).send(result);
    } catch (e) {

      debug(e);
      if (e.message == 'missing') {
        return res.status(404).send({ code: 404, status: 'No order has been made for this account' });
      }

      debug('error while geting inbox:%o', e.message);

      return res.status(500).send({ code: 500, status: 'Sorry!! Something went wrong please try later.' });
    }
  });

  return function getInbox(_x9, _x10) {
    return _ref5.apply(this, arguments);
  };
})();

let getOutbox = (() => {
  var _ref6 = _asyncToGenerator(function* (req, res) {

    try {
      let id = req.params.id;
      debug('Outbox id :%o ', id);

      let skip = 0;

      if (req.query.skip) {
        skip = req.query.skip;
      }

      let viewName = 'ToFrom/from-view';
      let options = { key: id, include_docs: true, reduce: false, group: false, limit: 1000, skip: skip };

      let response = yield orderDb.query(viewName, options);
      debug('Number of orders found: %o', response.rows.length);

      if (response.rows.length < 1) {
        return res.status(404).send({ code: 404, status: 'No order has been made for this account' });
      }

      let result = response.rows;

      let orgs = [];
      let orgsToName = {};

      for (let i = 0; i < response.rows.length; i++) {

        if (response.rows[i].doc.value.to) {
          orgs.push(response.rows[i].doc.value.to);
        }
      }

      debug('Getting org information for :%o', orgs);

      options = { keys: orgs, include_docs: true };
      response = yield orgDb.allDocs(options);

      for (let i = 0; i < response.rows.length; i++) {

        if (!response.rows[i].doc || !response.rows[i].doc.value || !response.rows[i].doc.value.name) {
          continue;
        }

        if (response.rows[i].doc.value.tag) {
          debug('Org tag :%o', response.rows[i].doc.value.tag);
          orgsToName[response.rows[i].id] = response.rows[i].doc.value.tag;
        }
      }

      debug("Org id to name : %o", orgsToName);

      for (let i = 0; i < result.length; i++) {

        if (result[i].doc.value.to) {
          result[i].doc.value.to = orgsToName[result[i].doc.value.to];
        }
      }

      return res.status(200).send(result);
    } catch (e) {
      if (e.message == 'missing') {
        return res.status(404).send({ message: 'No orders for this account' });
      }

      debug('error while geting outbox:%o', e.message);
      return res.status(500).send({ message: e.message });
    }
  });

  return function getOutbox(_x11, _x12) {
    return _ref6.apply(this, arguments);
  };
})();

let createOrder = (() => {
  var _ref7 = _asyncToGenerator(function* (req, res) {

    try {
      debug('Test body :', req.body);
      debug('Test body order :', req.body.order);
      let input = req.body.order;
      let first_org = req.body.to;
      let second_org = req.body.from;
      debug('Create order from :%o ', input.from);
      debug('Create order to :%o ', input.to);

      if (input.band > 2) {
        return res.status(403).send({ code: 403, status: 'You don\'t have access to this functionality' });
      }

      input.created = new Date().toISOString();
      input.status = 'created';
      if (req.account && req.account.value && req.account.value.name) {
        input.createdBy = req.account.value.name;
      }
      input.from = second_org.id;
      input.to = first_org.id;

      // Create order id from view

      let viewId = [input.to, input.from];
      let options = { key: viewId, include_docs: false, reduce: true, group: true };

      debug('Getting count from toFromCount view : %o', viewId);

      let response = yield orderDb.query(toFromCount, options);

      debug('Response from view toFromCount :%o', response);

      if (!response.rows) {
        throw new Error('Error while putting into db');
      }

      let count = 1;
      if (response.rows.length != 0) count = response.rows[0].value + 1;

      input.order_id = count;

      debug('New order :%o', JSON.stringify(input));
      response = yield orderDb.post({ value: input });
      debug('Response after posting order to db :%o', response);

      if (response.ok) {

        input.id = response.id;

        let accounts = yield _account2.default.getAllAccounts([[first_org.id, first_org.name, 1]], db);
        accounts.push((yield _account2.default.getAllAccounts([[first_org.id, first_org.name, 2]], db)));

        let notification_body = 'order_creation';

        let title_first_org = "New order from " + second_org.name;

        let type = 'inbox';

        let data = {};
        data.second_org = second_org;
        data.first_org = first_org;
        data.value = input;
        data.switch = notification_body;
        data.type = type;

        let subject = {};
        subject.body = title_first_org;

        accounts = _underscore2.default.flatten(accounts);

        debug('Accounts first part:%o', accounts);

        yield _notification2.default.send(accounts, data, subject);

        accounts = [];

        accounts.push((yield _account2.default.getAllAccounts([[second_org.id, second_org.name, 1]], db)));
        accounts.push((yield _account2.default.getAllAccounts([[second_org.id, second_org.name, 2]], db)));

        let title_second_org = "New order to " + first_org.name + " has been placed";

        type = 'outbox';
        data = {};
        data.second_org = second_org;
        data.first_org = first_org;
        data.value = input;
        data.switch = notification_body;
        data.type = type;

        accounts = _underscore2.default.flatten(accounts);

        debug('accounts second part:%o', accounts);

        subject = {};
        subject.body = title_second_org;

        yield _notification2.default.send(accounts, data, subject);

        return res.status(200).send(input);
      }

      throw new Error('error while putting into db');
    } catch (e) {
      debug('error while creating order:%o', e.message);
      return res.status(500).send({ code: 500, status: 'Sorry!! Something went wrong please try later.' });
    }
  });

  return function createOrder(_x13, _x14) {
    return _ref7.apply(this, arguments);
  };
})();

let putOrder = (() => {
  var _ref8 = _asyncToGenerator(function* (req, res) {

    try {

      let msgParam = false,
          band3 = true;

      if (!req.body.order || !req.body.to || !req.body.from) {
        return res.status(422).send('');
      }

      debug('put request order: %o', JSON.stringify(req.body.order));
      debug('put request to: %o', JSON.stringify(req.body.to));
      debug('put request from: %o', JSON.stringify(req.body.from));
      debug("put request edit query :%o", req.query.edit);
      debug("put request message query :%o", req.query.message);

      if (req.query.message) {

        debug('message query :%o', req.query.message);
        msgParam = req.query.message;
        band3 = false;
      }

      let body = req.body.order;
      let first_org = req.body.to;
      let second_org = req.body.from;

      debug("first_org :%o", first_org.id);
      debug("second_org :%o", second_org.id);

      let response = yield orderDb.get(body.id);

      debug('Got response from order get from db');

      if (msgParam == 'true') {

        if (!response.value.messages) {
          response.value.messages = [];
        }

        debug("Existing message length :%o", response.value.messages.length);
        debug("Adding new message :%o", body.messages);
        body.messages = _underscore2.default.extend(response.value.messages, body.messages);
      } else {

        if (req.query.edit == 'true') {
          body.status = 'created';
        } else {
          if (changeStatus[body.status] == undefined) {
            return res.status(400).send();
          }
          body.status = changeStatus[body.status];
          body[req.body.status] = new Date().toISOString();
        }
        if (req.account && req.account.value && req.account.value.name) {
          body[body.status + 'By'] = req.account.value.name;
        }
        if (body.status == 'created' || body.status == 'acknowledged') {
          band3 = false;
        }
      }

      body.last_update = new Date().toISOString();

      let doc = {};
      doc._id = body.id;
      doc._rev = response._rev;
      doc.value = body;
      doc.value.to = first_org.id;
      doc.value.from = second_org.id;
      delete doc.value.undefined;
      delete doc.value._id;
      delete doc.value.id;
      delete doc.value._rev;
      debug('Putting new doc with status %o', doc.value.status);
      response = yield orderDb.put(doc);
      body.from = second_org.id;
      body.id = doc._id;

      debug('Sending updated order');

      if (response.ok) {

        let accounts = yield _account2.default.getAllAccounts([[first_org.id, first_org.name, 1]], db);
        accounts.push((yield _account2.default.getAllAccounts([[first_org.id, first_org.name, 2]], db)));
        if (band3) {
          accounts.push((yield _account2.default.getAllAccounts([[first_org.id, first_org.name, 3]], db)));
        }

        let notification_body = 'order_detail_update';

        let title_first_org = "Order from " + second_org.name + " has been updated";

        let data = {};
        data.second_org = second_org;
        data.first_org = first_org;
        data.value = body;
        data.switch = notification_body;
        data.type = 'inbox';

        let subject = {};
        subject.body = title_first_org;

        accounts = _underscore2.default.flatten(accounts);

        debug('Accounts first part:%o', accounts);

        _notification2.default.send(accounts, data, subject);

        accounts = [];

        accounts.push((yield _account2.default.getAllAccounts([[second_org.id, second_org.name, 1]], db)));
        accounts.push((yield _account2.default.getAllAccounts([[second_org.id, second_org.name, 2]], db)));
        if (band3) {
          accounts.push((yield _account2.default.getAllAccounts([[second_org.id, second_org.name, 3]], db)));
        }

        let title_second_org = "Your order to " + first_org.name + " has been updated";

        data = {};
        data.second_org = second_org;
        data.first_org = first_org;
        data.value = body;
        data.switch = notification_body;
        data.type = 'outbox';

        accounts = _underscore2.default.flatten(accounts);

        debug('Accounts second part:%o', accounts);

        subject = {};
        subject.body = title_second_org;

        _notification2.default.send(accounts, data, subject);

        return res.status(200).send(body);
      }

      throw new Error('error while putting into db');
    } catch (e) {
      debug('error while putOrder:%o', e.message);
      return res.status(500).send({ message: e.message });
    }
  });

  return function putOrder(_x15, _x16) {
    return _ref8.apply(this, arguments);
  };
})();

let authorize = (() => {
  var _ref9 = _asyncToGenerator(function* (req, res, next) {
    try {

      debug('Authorizing');

      if (req.headers.authorization) {
        let response = yield db.get(req.headers.authorization);
        req.account = response;
        debug('Authorized');
        next();
      } else {
        debug('Unauthorized');
        return res.status(401).send('');
      }
    } catch (e) {

      if (e.message == 'missing' || e.message == 'deleted') {
        debug('Unauthorized');
        return res.status(401).send('');
      }

      debug('error in authorize :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function authorize(_x17, _x18, _x19) {
    return _ref9.apply(this, arguments);
  };
})();

let getTag = (() => {
  var _ref10 = _asyncToGenerator(function* (req, res) {

    try {

      let id = req.params.id;
      debug('getTag id :%o ', id);

      let viewName = tagView;
      let options = { keys: [id], include_docs: false, group: false, reduce: false };

      let response = yield orgDb.query(viewName, options);
      debug('account retrived :%o', response);

      if (response.rows.length == 0) {
        return res.status(404).send('');
      }

      return res.status(200).send('');
    } catch (e) {

      if (e.message == 'missing') {
        return res.status(404).send('');
      }

      debug('error in tag :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function getTag(_x20, _x21) {
    return _ref10.apply(this, arguments);
  };
})();

let performActionOnOrg = (() => {
  var _ref11 = _asyncToGenerator(function* (req, res) {

    try {

      debug("Body :%o", req.body);

      let response = yield _org2.default.performAction(req.body);

      if (response == '422') {

        return res.status(422).send('');
      } else if (response == '409') {

        return res.status(409).send('');
      } else if (response.message) {
        return res.status(200).send({ message: response.message });
      } else {

        let action, body;

        if (req.body.action == 'accept') {

          action = ' accpeted';
          body = 'paired_orgs';
        } else {

          body = 'pair_org_update';
          action = ' ignored';
        }

        let accounts = yield _account2.default.getAllAccounts([[req.body.first_org.id, req.body.first_org.name, 1]], db);
        accounts.push((yield _account2.default.getAllAccounts([[req.body.first_org.id, req.body.first_org.name, 2]], db)));

        let title_first_org = req.body.second_org.name + action + ' your request';

        let data = {};
        data.org_name = req.body.first_org.name;
        data.org_tag = req.body.first_org.tag;
        data.value = req.body.second_org;
        data.switch = body;

        let subject = {};
        subject.body = title_first_org;

        accounts = _underscore2.default.flatten(accounts);

        debug('accounts first part:%o', accounts);

        _notification2.default.send(accounts, data, subject);

        accounts = [];

        accounts.push((yield _account2.default.getAllAccounts([[req.body.second_org.id, req.body.second_org.name, 1]], db)));
        accounts.push((yield _account2.default.getAllAccounts([[req.body.second_org.id, req.body.second_org.name, 2]], db)));

        let title_second_org = "Your org " + req.body.second_org.name + action + " pair request to " + req.body.first_org.name;

        data = {};
        data.org_name = req.body.second_org.name;
        data.org_tag = req.body.second_org.tag;
        data.value = req.body.first_org;
        data.switch = body;

        accounts = _underscore2.default.flatten(accounts);

        debug('accounts second part:%o', accounts);

        if (req.account.value.name) {

          title_second_org = req.account.value.name + " from your organization " + req.body.second_org.name + action + " pair request from " + req.body.first_org.name;
        }

        subject = {};
        subject.body = title_second_org;

        _notification2.default.send(accounts, data, subject);

        return res.status(200).send('');
      }
    } catch (e) {

      debug('error in performActionOnOrg :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function performActionOnOrg(_x22, _x23) {
    return _ref11.apply(this, arguments);
  };
})();

let pairOrg = (() => {
  var _ref12 = _asyncToGenerator(function* (req, res) {

    try {

      if (!req.body.first_org || !req.body.second_org || !req.body.first_org.name || !req.body.second_org.name) {
        return res.status(400).send('Missing parameter.');
      }

      let response = yield _org2.default.pair(req.body.first_org, req.body.second_org);

      if (response == '409') {
        return res.status(409).send('');
      }

      if (response.message) {
        return res.status(200).send({ message: response.message });
      }

      let accounts = yield _account2.default.getAllAccounts([[req.body.first_org.id, req.body.first_org.name, 1]], db);
      accounts.push((yield _account2.default.getAllAccounts([[req.body.first_org.id, req.body.first_org.name, 2]], db)));

      let body = 'incoming_pair_org_request';
      let title_first_org = "Pair request from " + req.body.second_org.name;

      let data = {};
      data.org_name = req.body.first_org.name;
      data.org_tag = req.body.first_org.tag;
      data.value = req.body.second_org;
      data.switch = body;

      let subject = {};
      subject.body = title_first_org;

      accounts = _underscore2.default.flatten(accounts);

      debug('accounts first part:%o', accounts);

      _notification2.default.send(accounts, data, subject);

      accounts = [];

      accounts.push((yield _account2.default.getAllAccounts([[req.body.second_org.id, req.body.second_org.name, 1]], db)));
      accounts.push((yield _account2.default.getAllAccounts([[req.body.second_org.id, req.body.second_org.name, 2]], db)));

      let title_second_org = "Pair request sent to " + req.body.first_org.name;
      body = 'outgoing_pair_org_request';

      data = {};
      data.org_name = req.body.second_org.name;
      data.org_tag = req.body.second_org.tag;
      data.value = req.body.first_org;
      data.switch = body;

      accounts = _underscore2.default.flatten(accounts);

      debug('accounts second part:%o', accounts);

      if (req.account.value.name) {

        title_second_org = req.account.value.name + " from your organization " + req.body.second_org.name + " sent pair request to " + req.body.first_org.name;
      }

      subject = {};
      subject.body = title_second_org;

      _notification2.default.send(accounts, data, subject);

      return res.status(200).send('');
    } catch (e) {

      debug('error in pairOrg :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function pairOrg(_x24, _x25) {
    return _ref12.apply(this, arguments);
  };
})();

let addEmployee = (() => {
  var _ref13 = _asyncToGenerator(function* (req, res) {

    try {

      if (!req.body.phone_number || !req.body.org_name || !req.body.role || !req.body.org_id || !req.body.org_tag || req.body.phone_number.length < 10) {
        return res.status(400).send('Missing parameter.');
      }

      let response = yield _account2.default.isExist(req.body.phone_number, db);

      let orgDoc = {};
      orgDoc['id'] = req.body.org_id;
      orgDoc['band'] = parseInt(req.body.band);
      orgDoc['role'] = req.body.role;
      orgDoc['name'] = req.body.org_name;
      orgDoc['tag'] = req.body.org_tag;

      let name = req.account.value.name ? req.account.value.name : 'someone';

      debug('response from isExist :%o', response);
      let isOrgPresentAndConfirmFalse = false;

      if (response && response.length > 0 && response[0].doc && response[0].doc.value) {

        debug('Found account');

        if (!response[0].doc.value.orgs) {
          response[0].doc.value.orgs = [];
        } else {

          for (let i = 0; i < response[0].doc.value.orgs.length; i++) {

            if (response[0].doc.value.orgs[i].id == req.body.org_id && response[0].doc.value.confirm == false) {
              isOrgPresentAndConfirmFalse = true;
            }
            if (response[0].doc.value.orgs[i].id == req.body.org_id && response[0].doc.value.confirm == true) {
              debug('Sending 409 as org already exist');
              return res.status(409).send('');
            }
          }
        }

        if (!isOrgPresentAndConfirmFalse) {
          response[0].doc.value.orgs.push(orgDoc);
          debug('Updateing account');
          yield _account2.default.update(response[0].doc, db);
        }

        let dbOrgDoc = yield orgDb.get(req.body.org_id);
        let obj = {};
        if (dbOrgDoc._id && dbOrgDoc.value) {
          obj = dbOrgDoc.value;
          obj.id = dbOrgDoc._id;
          obj.band = parseInt(req.body.band);
          delete obj.pair;
        }

        let data = {};
        data.org_name = req.body.org_name;
        data.org_tag = req.body.org_tag;
        data.value = obj;
        data.switch = 'add_employee';

        let subject = {};
        subject.body = 'You have been added to ' + req.body.org_name + ' by ' + name + ' as ' + req.body.role;

        _notification2.default.send([response[0].id], data, subject);

        debug('sending message');

        let invitationResult = yield _notification2.default.inviteEmployee(req.body.phone_number, req.body.org_name, name);

        if (invitationResult.status && invitationResult.status >= 400 && invitationResult.status < 500) {
          return res.status(invitationResult.status).send('');
        } else {
          return res.status(500).send('');
        }

        return res.status(200).send();
      }

      let input = {};
      input.orgs = [];
      input.orgs.push(orgDoc);
      input.confirm = false;
      input.phone_number = req.body.phone_number;

      debug('creating account with :%o', input);
      let result = yield _account2.default.create(input, true, db);

      debug('sending message');
      let invitationResult = yield _notification2.default.inviteEmployee(req.body.phone_number, req.body.org_name, name);
      if (invitationResult.status && invitationResult.status >= 400 && invitationResult.status <= 500) {
        return res.status(invitationResult.status).send('');
      }
      return res.status(201).send('');
    } catch (e) {

      debug('error in addEmployee :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function addEmployee(_x26, _x27) {
    return _ref13.apply(this, arguments);
  };
})();

let updateOrg = (() => {
  var _ref14 = _asyncToGenerator(function* (req, res) {

    try {

      let newOrg = req.body;

      debug('updating org :%o', newOrg);

      let response = yield _org2.default.update(newOrg);

      if (response == '422') {
        return res.status(422).send('');
      }
      if (response == 'Org does not exist') {
        return res.status(404).send('');
      }

      newOrg = response.value;
      let pair = response.pair;
      let title;
      let body = 'org_update';

      if (!newOrg.name || !newOrg.id) {
        return res.status(400).send('');
      }

      title = 'Your organization ' + newOrg.name + ' updated information';

      let data = {};
      data.org_name = newOrg.name;
      data.org_tag = newOrg.tag;
      data.value = newOrg;
      data.switch = body;

      let subject = {};
      subject.body = title;

      yield sendNotification(subject, data, [1, 2, 3], newOrg);

      //get all pair prgs and send notification to all people

      if (!pair instanceof Array) {
        return res.status(200).send('');
      }

      title = 'Organization ' + newOrg.name + ' updated their information';

      for (let i = 0; i < pair.length; i++) {

        if (!pair[i].name) {
          continue;
        }

        body = 'pair_org_update';

        let data = {};
        data.org_name = pair[i].name;
        data.org_tag = pair[i].tag;
        data.value = newOrg;
        data.switch = body;

        let subject = {};
        subject.body = title;
        yield sendNotification(subject, data, [1, 2, 3], pair[i]);
      }

      debug('returning');

      return res.status(200).send('');
    } catch (e) {

      debug('error in updateOrg :%o', e.message);
      return res.status(500).send('');
    }
  });

  return function updateOrg(_x28, _x29) {
    return _ref14.apply(this, arguments);
  };
})();

let sendNotification = (() => {
  var _ref15 = _asyncToGenerator(function* (subject, data, bands, org) {

    try {

      let accounts = [];

      if (!bands instanceof Array || !org.id || !org.name) {
        return;
      }

      for (let i = 0; i < bands.length; i++) {
        accounts.push((yield _account2.default.getAllAccounts([[org.id, org.name, bands[i]]], db)));
      }

      accounts = _underscore2.default.flatten(accounts);

      _notification2.default.send(accounts, data, subject);
    } catch (e) {

      debug('error in sendNotification :%o', e.message);
      throw e;
    }
  });

  return function sendNotification(_x30, _x31, _x32, _x33) {
    return _ref15.apply(this, arguments);
  };
})();

var _dotenv = require('dotenv');

var _dotenv2 = _interopRequireDefault(_dotenv);

var _underscore = require('underscore');

var _underscore2 = _interopRequireDefault(_underscore);

var _abacusDebug = require('abacus-debug');

var _abacusDebug2 = _interopRequireDefault(_abacusDebug);

var _abacusExpress = require('abacus-express');

var _abacusExpress2 = _interopRequireDefault(_abacusExpress);

var _abacusDbclient = require('abacus-dbclient');

var _abacusDbclient2 = _interopRequireDefault(_abacusDbclient);

var _abacusPartition = require('abacus-partition');

var _abacusPartition2 = _interopRequireDefault(_abacusPartition);

var _pouchdb = require('pouchdb');

var _pouchdb2 = _interopRequireDefault(_pouchdb);

var _murmurhash = require('murmurhash');

var _murmurhash2 = _interopRequireDefault(_murmurhash);

var _email = require('./email');

var _email2 = _interopRequireDefault(_email);

var _helper = require('./helper');

var _helper2 = _interopRequireDefault(_helper);

var _jsonwebtoken = require('jsonwebtoken');

var _jsonwebtoken2 = _interopRequireDefault(_jsonwebtoken);

var _verify = require('./verify');

var _verify2 = _interopRequireDefault(_verify);

var _account = require('./account');

var _account2 = _interopRequireDefault(_account);

var _org = require('./org');

var _org2 = _interopRequireDefault(_org);

var _notification = require('./notification');

var _notification2 = _interopRequireDefault(_notification);

var _orgitems = require('./org/orgitems');

var orgitems = _interopRequireWildcard(_orgitems);

function _interopRequireWildcard(obj) { if (obj && obj.__esModule) { return obj; } else { var newObj = {}; if (obj != null) { for (var key in obj) { if (Object.prototype.hasOwnProperty.call(obj, key)) newObj[key] = obj[key]; } } newObj.default = obj; return newObj; } }

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _asyncToGenerator(fn) { return function () { var gen = fn.apply(this, arguments); return new Promise(function (resolve, reject) { function step(key, arg) { try { var info = gen[key](arg); var value = info.value; } catch (error) { reject(error); return; } if (info.done) { resolve(value); } else { return Promise.resolve(value).then(function (value) { step("next", value); }, function (err) { step("throw", err); }); } } return step("next"); }); }; }

const env = _dotenv2.default.config();

const dburl = process.env.DBURL || '';
const debug = (0, _abacusDebug2.default)('project');
const app = (0, _abacusExpress2.default)();
const accountDbName = process.env.ACCOUNTDB || 'account';
const ordersDbName = process.env.ORDERSDB; //|| 'order';
const activityDbName = process.env.ACTIVITYDB || 'activity';
const orgDbName = process.env.ORGDB || 'org';
const companyEmailAddress = process.env.COMPANYEMAIL;
const day = 86400000;
const tagView = 'orgDetails/byTag';
const toFromCount = 'ToFrom/toFrom-view';

let db, orderDb, activityDb, orgDb;
if (dburl === '') {
  debug('empty dburl, creating inmemory db');
  db = new _pouchdb2.default(dburl + accountDbName, { db: require('memdown') });
  orderDb = new _pouchdb2.default(dburl + ordersDbName, { db: require('memdown') });
  activityDb = new _pouchdb2.default(dburl + activityDbName, { db: require('memdown') });
} else {
  db = new _pouchdb2.default(dburl + '/' + accountDbName);
  orderDb = new _pouchdb2.default(dburl + '/' + ordersDbName);
  activityDb = new _pouchdb2.default(dburl + '/' + activityDbName);
  orgDb = new _pouchdb2.default(dburl + '/' + orgDbName);
}

let status = ['created', 'acknowledged', 'shipping', 'sender_completed', 'receiver_completed', 'cancelled'];
let changeStatus = {};
changeStatus.created = 'acknowledged';
changeStatus.acknowledged = 'shipping';
changeStatus.shipping = 'sender_completed';
changeStatus.cancelled = 'cancelled';
changeStatus.sender_completed = 'receiver_completed';

debug('dbUrl :%o', dburl);

app.post('/onboard', onBoard);

app.get('/account/:id/orgs', authorize, getOrgs);
app.post('/add/employee', authorize, addEmployee);
app.put('/account/:id', authorize, updateAccount);

app.get('/tag/:id', authorize, getTag);
app.get('/search/org/:id', authorize, _org2.default.search);
app.get('/org/pair/pending/:id', authorize, _org2.default.getPendingPair);
app.get('/org/pair/requested/:id', authorize, _org2.default.getRequestedPair);
app.get('/org/pair/:id', authorize, _org2.default.getPairedOrgs);
app.post('/org/employee/phone_number', authorize, _account2.default.getAllAccountsByOrgs);
app.get('/org/:id/OrganizationItems', authorize, orgitems.get);
app.post('/org/:id/OrganizationItems', authorize, orgitems.post);
app.get('/paired_orgs/:id', authorize, _org2.default.getPairedOrgs);

app.post('/org', authorize, createOrg);
app.put('/org/:id', authorize, updateOrg);
app.post('/org/pair/:id', authorize, pairOrg);
app.post('/org/pair/action/:id', authorize, performActionOnOrg);

app.get('/org/:id/Inbox', authorize, getInbox);
app.get('/org/:id/Outbox', authorize, getOutbox);
app.post('/org/:id/order', authorize, createOrder);
app.put('/order/:oid', authorize, putOrder);

app.get('/authy/verify/:cc/:phn', _verify2.default.phone);
app.get('/authy/verify/code', _verify2.default.code);

app.put('/firebaseToken/:id', authorize, _notification2.default.updateToken);
app.put('/record/activity/:id', authorize, _account2.default.recordActivity);
app.post('/feedback', authorize, _account2.default.feedback);

app.get('/', (req, res) => res.status(200).send('Hello World!'));

// let port = 5001;
app.listen(process.env.PORT || 5001);