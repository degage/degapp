import update from 'immutability-helper'
import { findIndex } from 'lodash/findIndex'
import { CREATED_MEMBERSHIP_INVOICE, CREATING_MEMBERSHIP_INVOICE, CREATING_MEMBERSHIP_INVOICE_FAILED } from '../actions/user';
import Cookies from 'universal-cookie'

const cookies = new Cookies()

export const UsersStatuses = {
  INIT: 'INIT',
  FETCHED_USER: 'FETCHED_USER',
  FETCHING_USER: 'FETCHING_USER',
  FETCHING_USER_FAILED: 'FETCHING_USER_FAILED',
  FETCHED_USERS: 'FETCHED_USERS',
  FETCHING_USERS: 'FETCHING_USERS',
  FETCHING_USERS_FAILED: 'FETCHING_USERS_FAILED',
  SET_FILTER_USERS: 'SET_FILTER_USERS',
  SET_PAGE_USERS: 'SET_PAGE_USERS',
  SET_SORT_ORDER_USERS: 'SET_SORT_ORDER_USERS',
  USERS_CLOSE_MODAL: 'USERS_CLOSE_MODAL',
  USERS_OPEN_MODAL: 'USERS_OPEN_MODAL',
  SET_PAGESIZE_USERS: 'SET_PAGESIZE_USERS',
  FETCHED_USER_STATS: 'FETCHED_USER_STATS',
  FETCHING_USER_STATS: 'FETCHING_USER_STATS',
  FETCHING_USER: 'FETCHING_USER'
}

export const userReducer = (state = {
  user: {},
  suggestions: [],
  users: [],
  status: UsersStatuses.INIT,
  view: {
    isUserModalOpen: false,
    isUserPickerModalOpen: false,
    users: {
      table: {
        page: 1,
        pageSize: cookies.get('u:ps') != null ? cookies.get('u:ps') : 10,
        fullSize: 0,
        filter: '',
        orderBy: cookies.get('u:orderBy') != null ? cookies.get('u:orderBy') : 'name',
        asc: cookies.get('u:asc') != null ? cookies.get('u:asc') : 0,
        showPagination: true,
        showFilter: true,
        columns: [
          {label: 'Gebruiker', sortField: 'name'},
          {label: 'Betalingsplan', sortField: 'plan'},
          {label: 'Ontvangt rappels', sortField: null}
        ]
      }
    },
    hasSuggestionChanged: false,
    suggestion: {
      firstName: '',
      lastName: ''
    },
    suggestionText: '',
    suggestions: {
      table: {
        fullSize: 0
      }
    }
  }
}, action) => {
  switch (action.type) {
    case 'FETCHED_USER':
      return update(state, {
        user: {
          accountNumber: {$set: action.user.accountNumber},
          addressDomicile: {$set: action.user.addressDomicile},
          addressResidence: {$set: action.user.addressResidence},
          cellPhone: {$set: action.user.cellPhone},
          creditStatus: {$set: action.user.creditStatus},
          dateJoined: {$set: action.user.dateJoined},
          degageId: {$set: action.user.degageId},
          email: {$set: action.user.email},
          firstName: {$set: action.user.firstName},
          id: {$set: action.user.id},
          lastName: {$set: action.user.lastName},
          license: {$set: action.user.license},
          phone: {$set: action.user.phone},
          sendReminder: {$set: action.user.sendReminder},
          status: {$set: action.user.status},
        }
      })
    case 'FETCHED_USER_STATS':
      return update(state, {
        user: {
          userStats: {$set: action.userStats.user},
          amountToPay: {$set: action.userStats.amountToPay},
          amountPaid: {$set: action.userStats.amountPaid}
        }
      })
    case 'SHOW_USER':
      return update(state, {
        view: {
          isUserModalOpen: {$set: true}
        }
      })
    case 'HIDE_USER':
      return update(state, {
        view: {
          isUserModalOpen: {$set: false}
        }
      })
    case 'SHOW_USER_PICKER':
      return update(state, {
        view: {
          isUserPickerModalOpen: {$set: true}
        }
      })
    case 'HIDE_USER_PICKER':
      return update(state, {
        view: {
          isUserPickerModalOpen: {$set: false}
        }
      })
    case 'SET_USER_SUGGESTION':
      return update(state, {
        view: {
          suggestion: {$set: action.user},
          hasSuggestionChanged: {$set: state.view.suggestion != null && state.view.suggestion.id != null && action.user != null && action.user.id != state.view.suggestion.id}
        }
      })
    case 'SET_USER_SUGGESTION_ID':
      const suggestion = state.suggestions.find(sugg => sugg.id == action.userId)
      return update(state, {
        view: {
          hasSuggestionChanged: {$set: true},
          suggestion: {$set: suggestion}
        }
      })
    case 'SET_USER_SUGGESTION_TEXT':
      return update(state, {
        view: {
          suggestionText: {$set: action.suggestionText}
        }
      })
    case 'FETCHED_USER_SUGGESTIONS':
      return update(state, {
          suggestions: {$set: action.users},
          view: {
            suggestions: {
              table: {
                fullSize: {$set: action.fullSize}
              }
            }
          },
          status: {$set: UsersStatuses.FETCHED_USERS},
        })
    case 'FETCHING_USERS':
      return update(state, {
          status: {$set: UsersStatuses.FETCHING_USERS},
        })
    case 'FETCHING_USERS_FAILED':
      return update(state, {
        status: {$set: UsersStatuses.FETCHING_USERS_FAILED},
      })
    case 'FETCHED_USERS':
      return update(state, {
        users: {$set: action.users},
        view: {
          users: {
            table: {
              fullSize:{$set: action.fullSize},
              pageSize: {$set: action.pageSize}
            }
          }
        },
        status: {$set: UsersStatuses.FETCHED_USER},
      })
      case 'FETCHING_USER':
        return update(state, {
          status: {$set: UsersStatuses.FETCHING_USER},
      })
      case 'FETCHING_USER_STATS':
        return update(state, {
          status: {$set: UsersStatuses.FETCHING_USER_STATS},
      })
    case 'SET_FILTER_USERS':
      return update(state, {
        view: {
          users: {
            table: {
              filter: {$set: action.filter},
              page: {$set: '1'}
            }
          }
        }
      })
      case 'SET_SORT_ORDER_USERS':
        return update(state, {
          view: {
            users: {
              table: {
                orderBy: {$set: action.orderBy},
                asc: {$set: action.asc}
              }
            }
          }
        })
      case 'SET_PAGE_USERS':
        return update(state, {
          view: {
            users: {
              table: {
                page: {$set: action.page}
              }
            }
          }
        })
      case 'USERS_OPEN_MODAL':
        return update(state, {
          view: {
            isUserModalOpen: {$set: true}
          },
          users: {$set: []},
          userSearch: {$set: ''}
        })
      case 'USERS_CLOSE_MODAL':
        return update(state, {
          view: {
            isUserModalOpen: {$set: false}
          }
        })
      case 'SET_PAGESIZE_USERS':
        return update(state, {
          view: {
            users: {
              table: {
                pageSize: {$set: action.pageSize}
              }
            }
          }
        })
      case CREATED_MEMBERSHIP_INVOICE:
        return update(state, {
          status: {$set: UsersStatuses.FETCHED_USER},
        })      
      case CREATING_MEMBERSHIP_INVOICE:
        return update(state, {
          status: {$set: UsersStatuses.FETCHING_USER},
        })      
      case CREATING_MEMBERSHIP_INVOICE_FAILED:
        return update(state, {
          status: {$set: UsersStatuses.FETCHING_USER_FAILED},
        })      
    default:
      return state;
  }
};
