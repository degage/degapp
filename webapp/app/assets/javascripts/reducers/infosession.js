import update from 'immutability-helper'
import { findIndex } from 'lodash/findIndex'

export const InfosessionsStatuses = {
  INIT: 'INIT',
  FETCHED_INFOSESSIONS: 'FETCHED_INFOSESSIONS',
  FETCHING_INFOSESSIONS: 'FETCHING_INFOSESSIONS',
  FETCHING_INFOSESSIONS_FAILED: 'FETCHING_INFOSESSIONS_FAILED',
  SELECTING_INFOSESSION: 'SELECTING_INFOSESSION',
  SET_PAGE_INFOSESSIONS: 'SET_PAGE_INFOSESSIONS',
  EDITING_STATUS: 'EDITING_STATUS',
  SET_FILTER_INFOSESSIONS: 'SET_FILTER_INFOSESSIONS',
  SET_PAGESIZE_INFOSESSIONS: 'SET_PAGESIZE_INFOSESSIONS',
  FETCHING_ATTENDING_INFOSESSIONS_FAILED: 'FETCHING_ATTENDING_INFOSESSIONS_FAILED',
  FETCHING_ATTENDING_INFOSESSIONS: 'FETCHING_ATTENDING_INFOSESSIONS',
  FETCHED_ATTENDING_INFOSESSIONS: 'FETCHED_ATTENDING_INFOSESSIONS'

}

export const InfosessionStatuses = {
}

export const infosessionReducer = (state = {
  infosessions: [],
  view: {
    isInfosessionModalOpen: false,
    infosession: {
      status: InfosessionStatuses.INACTIVE
    },
    attending: {},
    table: {
      page: 1,
      pageSize: 10,
      fullSize: 0,
      filter: '',
      showFilter: false,
      orderBy: 'date',
      asc: 0,
      showPagination: false,
      columns: [
        {label: 'Type', sortField: 'type'},
        {label: 'Tijdstip', sortField: 'date'},
        {label: 'Inschrijvingen', sortField: 'inschrijvingen'},
        {label: 'Gastvrouw / -heer', sortField: 'host'},
        {label: 'Adres', sortField: 'adress'},
        {label: 'Acties', sortField: 'acties'}
      ]
    }
  }
}, action) => {
  switch (action.type) {
    case 'INFOSESSIONS_OPEN_MODAL':
      return update(state, {
        view: {
          isSelectInfosessionModalOpen: {$set: true}
        },
        infosessions: {$set: []},
        infosessionSearch: {$set: ''}
      })
    case 'INFOSESSIONS_CLOSE_MODAL':
      return update(state, {
        view: {
          isSelectInfosessionModalOpen: {$set: false}
        }
      })
    case 'FETCHED_INFOSESSIONS':
      return update(state, {
        infosessions: {$set: action.infosessions},
        status: {$set: InfosessionsStatuses.FETCHED_INFOSESSIONS}
      })
    case 'FETCHING_INFOSESSIONS':
      return update(state, {
        status: {$set: InfosessionsStatuses.FETCHING_INFOSESSIONS},
      })
    case 'SELECTING_INFOSESSION':
      return update(state, {
        status: {$set: InfosessionsStatuses.SELECTING_INFOSESSION},
        paymentId: {$set: action.paymentId},
        view: {
          isSelectInfosessionModalOpen: {$set: true}
        }
      })
    case 'FETCHED_INFOSESSION':
      return update(state, {
        infosession: {$set: action.infosession}
      })
    case 'SHOW_INFOSESSION':
      return update(state, {
        view: {
          isInfosessionModalOpen: {$set: true}
        }
      })
    case 'HIDE_INFOSESSION':
      return update(state, {
        view: {
          isInfosessionModalOpen: {$set: false}
        }
      })
    case 'HIDE_SELECT_INFOSESSION':
      return update(state, {
        view: {
          isSelectInfosessionModalOpen: {$set: false}
        }
      })
      case 'FETCHED_ATTENDING_INFOSESSIONS':
        return update(state, {
          attending:  {$set: action.attending},
          view: {
            status: {$set: InfosessionsStatuses.FETCHED_ATTENDING_INFOSESSIONS}
          }
        })
        case 'FETCHING_ATTENDING_INFOSESSIONS':
          return update(state, {
            view: {
              status: {$set: InfosessionsStatuses.FETCHING_ATTENDING_INFOSESSIONS},
            }
          })
          case 'FETCHING_ATTENDING_INFOSESSIONS_FAILED':
            return update(state, {
              view: {
                status: {$set: InfosessionsStatuses.FETCHING_ATTENDING_INFOSESSIONS_FAILED},
              }
            })
    default:
      return state;
  }
};
