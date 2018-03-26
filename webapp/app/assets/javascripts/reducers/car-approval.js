import update from 'immutability-helper'
import { findIndex } from 'lodash/findIndex'
import Cookies from 'universal-cookie'

const cookies = new Cookies()

export const CarApprovalsStatuses = {
  FETCHED_CAR_APPROVALS: 'FETCHED_CAR_APPROVALS',
  FETCHING_CAR_APPROVALS: 'FETCHING_CAR_APPROVALS',
  FETCHING_CAR_APPROVALS_FAILED: 'FETCHING_CAR_APPROVALS_FAILED',
  SET_PAGE: 'SET_PAGE',
  SET_FILTER_CAR_APPROVALS: 'SET_FILTER_CAR_APPROVALS',
  SET_PAGESIZE_CAR_APPROVALS: 'SET_PAGESIZE_CAR_APPROVALS',
  FETCHING_CAR_ADMINS: 'FETCHING_CAR_ADMINS',
  FETCHED_CAR_ADMINS: 'FETCHED_CAR_ADMINS',
  FETCHING_CAR_ADMINS_FAILED: 'FETCHING_CAR_ADMINS_FAILED'
}

export const carApprovalReducer = (state = {
  carApprovals: [],
  carAdmins: [],
  selectedCarAdmin: null,
  view: {
    carApprovals: {
      table: {
        page: 1,
        pageSize: cookies.get('carappr:ps') != null ? cookies.get('carappr:ps') : 10,
        fullSize: 0,
        filter: '',
        status: 'REQUEST',
        showFilter: true,
        orderBy: cookies.get('carappr:orderBy') != null ? cookies.get('carappr:orderBy') : 'CAR_ID',
        asc: cookies.get('carappr:asc') != null ? cookies.get('carappr:asc') : 0,
        showPagination: true,
        columns: [
          {label: '', sortField: 'CAR_ID'},
          {label: 'Naam', sortField: 'NAME'},
          {label: 'Eigenaar', sortField: 'OWNER'},
          {label: 'Infosessie', sortField: 'STATUS'},
          {label: 'Laatste aanpassing', sortField: 'CAR_CREATION_DATE'},
          {label: 'Admin', sortField: 'CAR_ADMIN'},
          {label: 'Acties', sortField: null}
        ]
      }
    },
    isApproveModalOpen: false,
    approvalModalActionType: null,
    carApprovalId: null
  }
}, action) => {
  switch (action.type) {
    case 'FETCHED_CAR_APPROVALS':
      return update(state, {
          carApprovals: {$set: action.carApprovals.base},
          view: {
            carApprovals: {
              table: {
                fullSize: {$set: action.carApprovals.fullSize}
              }
            }
          },
          status: {$set: CarApprovalsStatuses.FETCHED_CAR_APPROVALS}
        })
    case 'FETCHING_CAR_APPROVALS':
      return update(state, {
          status: {$set: CarApprovalsStatuses.FETCHING_CAR_APPROVALS}
        })
    case 'FETCHING_CAR_APPROVALS_FAILED':
      return update(state, {
        status: {$set: CarApprovalsStatuses.FETCHING_CAR_APPROVALS_FAILED}
      })
    case 'FETCHED_CAR_ADMINS':
      return update(state, {
        carAdmins: { $set: action.carAdmins.base },
        status: { $set: CarApprovalsStatuses.FETCHED_CAR_ADMINS }
      })
    case 'FETCHING_CAR_ADMINS':
      return update(state, {
        status: { $set: CarApprovalsStatuses.FETCHING_CAR_ADMINS }
      })
    case 'FETCHING_CAR_ADMINS_FAILED':
      return update(state, {
        status: { $set: CarApprovalsStatuses.FETCHING_CAR_ADMINS_FAILED }
      })
    case 'SORTING_CAR_APPROVALS':
      return update(state, {
        status: {$set: CarApprovalsStatuses.SORTING_CAR_APPROVALS}
      })
    case 'SORTED_CAR_APPROVALS':
      return update(state, {
        status: {$set: CarApprovalsStatuses.SORTED_CAR_APPROVALS},
        carApprovals: {$set: action.carApprovals}
      })
    case 'SORTING_CAR_APPROVALS_FAILED':
      return update(state, {
        status: {$set: CarApprovalsStatuses.SORTING_CAR_APPROVALS_FAILED}
      })
    case 'SET_PAGE_CAR_APPROVALS':
      return update(state, {
        view: {
          carApprovals: {
            table: {
              page: {$set: action.page}
            }
          }
        }
      })
    case 'SET_SORT_ORDER_CAR_APPROVALS':
      return update(state, {
        view: {
          carApprovals: {
            table: {
              orderBy: {$set: action.orderBy},
              asc: {$set: action.asc}
            }
          }
        }
      })
    case 'SET_FILTER_CAR_APPROVALS':
      return update(state, {
        view: {
          carApprovals: {
            table: {
              filter: {$set: action.filter},
              page: {$set: '1'}
            }
          }
        }
      })
    case 'SET_STATUS_CAR_APPROVALS':
      return update(state, {
        view: {
          carApprovals: {
            table: {
              status: { $set: action.status },
              page: { $set: '1' }
            }
          }
        }
      })
    case 'SET_PAGESIZE_CAR_APPROVALS':
      return update(state, {
        view: {
          carApprovals: {
            table: {
              pageSize: {$set: action.pageSize}
            }
          }
        }
      })

    case 'CHANGE_APPROVAL_FIELD':
      return update(state, {
        carApprovals: {
          $apply: carApprovals => carApprovals.map((carApproval) => {
            if (carApproval.id === action.carApprovalId) {
              return { ...carApproval, [action.fieldName]: action.fieldValue }
            } else {
              return carApproval
            }
          })
        }
      })

    case 'OPEN_APPROVAL_MODAL':
      return update(state, {
        view: {
          isApproveModalOpen: { $set: true },
          carApprovalId: { $set: action.carApprovalId },
          approvalModalActionType: { $set: action.actionType }
        }
      })

    case 'CLOSE_APPROVAL_MODAL':
      return update(state, {
        view: {
          isApproveModalOpen: { $set: false },
          carApprovalId: { $set: null },
          approvalModalActionType: { $set: null }
        },
        selectedCarAdmin: { $set: null }
      })
    
    case 'CHANGE_SELECTED_ADMIN':
      return update(state, {
        selectedCarAdmin: { $set: action.selectedId }
      })

    default:
      return state;
  }
};
