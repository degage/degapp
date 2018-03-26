import update from 'immutability-helper'

export const Routes = {
  INVOICES: 'INVOICES',
  PAYMENTS: 'PAYMENTS',
  USER_STATISTICS: 'USER_STATISTICS',
  QUARTER_STATISTICS: 'QUARTER_STATISTICS',
  CODA_UPLOAD: 'CODA_UPLOAD',
  REMINDERS: 'REMINDERS',
  TRIPS: 'TRIPS',
  CAR_REGISTERED: 'CAR_REGISTERED',
  CAR_VALIDATING: 'CAR_VALIDATING',
  CAR_FULL: 'CAR_FULL',
  CAR_APPROVAL_REQUEST: 'REQUEST',
  CAR_APPROVAL_ACCEPTED: 'ACCEPTED',
  CAR_APPROVAL_REFUSED: 'REFUSED',
  USERS: 'USERS'
}

export const UsersStatuses = {
  SELECT_ROUTE: 'SELECT_ROUTE'
}

export const navigationReducer = (state = {view: {route: Routes.INVOICES}
}, action) => {
  switch (action.type) {
    case 'SELECT_ROUTE':
      return update(state, {
        view: {
          route: {$set: action.route}
        }
      })
    default:
      return state;
  }
};
