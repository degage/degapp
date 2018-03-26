import update from 'immutability-helper'
import { findIndex } from 'lodash/findIndex'
import Cookies from 'universal-cookie'

const cookies = new Cookies()

export const UserStatisticsStatuses = {
  FETCHED_USER_STATISTICS: 'FETCHED_USER_STATISTICS',
  FETCHING_USER_STATISTICS: 'FETCHING_USER_STATISTICS',
  FETCHING_USER_STATISTICS_FAILED: 'FETCHING_USER_STATISTICS_FAILED',
  SET_PAGE_USER_STATISTICS: 'SET_PAGE_USER_STATISTICS',
  SET_FILTER_USER_STATISTICS: 'SET_FILTER_USER_STATISTICS',
  SET_PAGESIZE_USER_STATISTICS: 'SET_PAGESIZE_USER_STATISTICS'
}

export const userStatisticsReducer = (state = {userStatistics: [],
  view: {
    userStatistics: {
      table: {
        page: 1,
        pageSize: cookies.get('ustat:ps') != null ? cookies.get('ustat:ps') : 10,
        fullSize: 0,
        filter: '',
        showFilter: true,
        showPagination: true,
        orderBy: cookies.get('ustat:orderBy') != null ? cookies.get('ustat:orderBy') : null,
        asc: cookies.get('ustat:asc') != null ? cookies.get('ustat:asc') : 1,
        columns: [
          {label: 'Gebruiker', sortField: 'name'},
          {label: 'Te betalen', sortField: null},
          {label: 'Betaald', sortField: null},
          {label: 'Verschil', sortField: null}
        ]
      }
    }
  }
}, action) => {
  switch (action.type) {
    case 'FETCHED_USER_STATISTICS':
      return update(state, {
          userStatistics: {$set: action.userStatistics.base},
          view: {
            userStatistics: {
              table: {
                fullSize: {$set: action.userStatistics.fullSize}
              }
            }
          },
          status: {$set: UserStatisticsStatuses.FETCHED_USER_STATISTICS},
        })
    case 'FETCHING_USER_STATISTICS':
      return update(state, {
          status: {$set: UserStatisticsStatuses.FETCHING_USER_STATISTICS},
        })
    case 'FETCHING_USER_STATISTICS_FAILED':
      return update(state, {
        status: {$set: UserStatisticsStatuses.FETCHING_USER_STATISTICS_FAILED},
      })
    case 'SET_PAGE_USER_STATISTICS':
      return update(state, {
        view: {
          userStatistics: {
            table: {
              page: {$set: action.page}
            }
          }
        }
      })
    case 'SET_SORT_ORDER_USER_STATISTICS':
      return update(state, {
        view: {
          userStatistics: {
            table: {
              orderBy: {$set: action.orderBy},
              asc: {$set: action.asc}
            }
          }
        }
      })
    case 'SET_FILTER_USER_STATISTICS':
      return update(state, {
        view: {
          userStatistics: {
            table: {
              filter: {$set: action.filter},
              page: {$set: '1'}
            }
          }
        }
      })
      case 'SET_PAGESIZE_USER_STATISTICS':
        return update(state, {
          view: {
            userStatistics: {
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
