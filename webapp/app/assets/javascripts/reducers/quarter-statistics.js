import update from 'immutability-helper'
import { findIndex } from 'lodash/findIndex'
import Cookies from 'universal-cookie'

const cookies = new Cookies()

export const QuarterStatisticsStatuses = {
  FETCHED_QUARTER_STATISTICS: 'FETCHED_QUARTER_STATISTICS',
  FETCHING_QUARTER_STATISTICS: 'FETCHING_QUARTER_STATISTICS',
  FETCHING_QUARTER_STATISTICS_FAILED: 'FETCHING_QUARTER_STATISTICS_FAILED',
  SORTING_QUARTER_STATISTICS: 'SORTING_QUARTER_STATISTICS',
  SORTING_QUARTER_STATISTICS_FAILED: 'SORTING_QUARTER_STATISTICS_FAILED',
  SET_PAGE: 'SET_PAGE',
  SET_PAGESIZE_QUARTER_STATISTICS: 'SET_PAGESIZE_QUARTER_STATISTICS'
}

export const quarterStatisticsReducer = (state = {quarterStatistics: [],
  view: {
    quarterStatistics: {
      table: {
        page: 1,
        pageSize: 10,
        fullSize: 0,
        filter: '',
        showFilter: false,
        orderBy: cookies.get('qstat:orderBy') != null ? cookies.get('qstat:orderBy') : null,
        asc: cookies.get('qstat:asc') != null ? cookies.get('qstat:asc') : 0,
        columns: [
          {label: 'Kwartaal', sortField: null},
          {label: 'Afrekeningen open', sortField: null},
          {label: 'Afrekeningen betaald', sortField: null},
          {label: 'Afrekeningen te laat', sortField: null},
          {label: 'Ontvangen bedrag', sortField: null},
          {label: 'Nog te ontvangen bedrag', sortField: null},
          {label: 'Betaald bedrag (door degage)', sortField: null},
          {label: 'Nog te betalen bedrag (door degage)', sortField: null},
          {label: 'Gemiddelde betalingstijd (dagen)', sortField: null}
        ]
      }
    }
  }
}, action) => {
  switch (action.type) {
    case 'FETCHED_QUARTER_STATISTICS':
      return update(state, {
          quarterStatistics: {$set: action.quarterStatistics},
          status: {$set: QuarterStatisticsStatuses.FETCHED_QUARTER_STATISTICS},
        })
    case 'FETCHING_QUARTER_STATISTICS':
      return update(state, {
          status: {$set: QuarterStatisticsStatuses.FETCHING_QUARTER_STATISTICS},
        })
    case 'FETCHING_QUARTER_STATISTICS_FAILED':
      return update(state, {
        status: {$set: QuarterStatisticsStatuses.FETCHING_QUARTER_STATISTICS_FAILED},
      })
    case 'SORTING_QUARTER_STATISTICS':
      return update(state, {
        status: {$set: QuarterStatisticsStatuses.SORTING_QUARTER_STATISTICS}
      })
    case 'SORTED_QUARTER_STATISTICS':
    return update(state, {
      status: {$set: QuarterStatisticsStatuses.SORTED_QUARTER_STATISTICS},
      quarterStatistics: {$set: action.quarterStatistics},
    })
    case 'SORTING_QUARTER_STATISTICS_FAILED':
      return update(state, {
        status: {$set: QuarterStatisticsStatuses.SORTING_QUARTER_STATISTICS_FAILED},
      })
    case 'SET_PAGE':
      return update(state, {
        view: {
          quarterStatistics: {
            table: {
              page: {$set: action.page}
            }
          }
        }
      })
    case 'SET_SORT_ORDER':
      return update(state, {
        view: {
          quarterStatistics: {
            table: {
              orderBy: {$set: action.orderBy},
              asc: {$set: action.asc}
            }
          }
        }
      })
    case 'SET_FILTER':
      return update(state, {
        view: {
          quarterStatistics: {
            table: {
              filter: {$set: action.filter}
            }
          }
        }
      })
      case 'SET_PAGESIZE_QUARTER_STATISTICS':
        return update(state, {
          view: {
            quarterStatistics: {
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
