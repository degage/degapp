import update from 'immutability-helper'
import { findIndex } from 'lodash/findIndex'
import Cookies from 'universal-cookie'

const cookies = new Cookies()

export const RemindersStatuses = {
  INIT: 'INIT',
  FETCHED_REMINDERS: 'FETCHED_REMINDERS',
  FETCHING_REMINDERS: 'FETCHING_REMINDERS',
  FETCHING_REMINDERS_FAILED: 'FETCHING_REMINDERS_FAILED',
  SET_PAGE_REMINDERS: 'SET_PAGE_REMINDERS',
  SET_FILTER_REMINDERS: 'SET_FILTER_REMINDERS',
  SET_PAGESIZE_REMINDERS: 'SET_PAGESIZE_REMINDERS',
  CREATING_REMINDERS: 'CREATING_REMINDERS',
  CREATING_REMINDERS: 'CREATING_REMINDERS',
  CREATING_REMINDERS_FAILED: 'CREATING_REMINDERS_FAILED',
  MAILED_REMINDER: 'MAILED_REMINDER',
  MAILING_REMINDER: 'MAILING_REMINDER',
  MAILING_REMINDER_FAILED: 'MAILING_REMINDER_FAILED'
}

export const remindersReducer = (state = {reminders: [],
  view: {
    reminders: {
      table: {
        page: 1,
        pageSize: cookies.get('remind:ps') != null ? cookies.get('remind:ps') : 10,
        fullSize: 0,
        filter: '',
        showFilter: true,
        showPagination: true,
        orderBy: cookies.get('remind:orderBy') != null ? cookies.get('remind:orderBy') : 'sent_on',
        asc: cookies.get('remind:asc') != null ? cookies.get('remind:asc') : 0,
        columns: [
          {label: 'Nummer', sortField: 'number'},
          {label: 'Gebruiker', sortField: 'name'},
          {label: 'Beschrijving', sortField: 'description'},
          {label: 'Datum', sortField: 'date'},
          {label: 'Status', sortField: 'status'},
          {label: 'Afrekening', sortField: 'invoice'},
          {label: 'Bedrag', sortField: null},
          {label: 'Verzonden op', sortField: 'sent_on'},
          {label: '', sortField: null}
        ]
      }
    },
    createReminders: {
      status: RemindersStatuses.INIT
    }
  }
}, action) => {
  switch (action.type) {
    case 'FETCHED_REMINDERS':
      return update(state, {
          reminders: {$set: action.reminders.base},
          view: {
            reminders: {
              table: {
                fullSize: {$set: action.reminders.fullSize}
              }
            }
          },
          status: {$set: RemindersStatuses.FETCHED_REMINDERS},
        })
    case 'FETCHING_REMINDERS':
      return update(state, {
          status: {$set: RemindersStatuses.FETCHING_REMINDERS},
        })
    case 'MAILING_REMINDER':
      return update(state, {
          status: {$set: RemindersStatuses.MAILING_REMINDER},
        })
    case 'MAILED_REMINDER':
      const reminders1 = state.reminders.map(reminder =>
        reminder.reminder.id === action.reminder.reminder.id ? action.reminder : reminder
      )
      return update(state, {
        status: {$set: RemindersStatuses.MAILED_REMINDER},
        reminders: {$set: reminders1}
      })
    case 'FETCHING_REMINDERS_FAILED':
      return update(state, {
        status: {$set: RemindersStatuses.MAILING_REMINDER_FAILED},
      })
    case 'FETCHING_REMINDERS_FAILED':
      return update(state, {
        status: {$set: RemindersStatuses.FETCHING_REMINDERS_FAILED},
      })
    case 'SET_PAGE_REMINDERS':
      return update(state, {
        view: {
          reminders: {
            table: {
              page: {$set: action.page}
            }
          }
        }
      })
    case 'SET_SORT_ORDER_REMINDERS':
      return update(state, {
        view: {
          reminders: {
            table: {
              orderBy: {$set: action.orderBy},
              asc: {$set: action.asc}
            }
          }
        }
      })
    case 'SET_FILTER_REMINDERS':
      return update(state, {
        view: {
          reminders: {
            table: {
              filter: {$set: action.filter},
              page: {$set: '1'}
            }
          }
        }
      })
      case 'SET_PAGESIZE_REMINDERS':
        return update(state, {
          view: {
            reminders: {
              table: {
                pageSize: {$set: action.pageSize}
              }
            }
          }
        })
    default:
      return state;
  }
};
