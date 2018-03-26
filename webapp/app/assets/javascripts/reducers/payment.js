import update from 'immutability-helper'
import { findIndex } from 'lodash/findIndex'
import Cookies from 'universal-cookie'

const cookies = new Cookies()

export const PaymentsStatuses = {
  INIT: 'INIT',
  FETCHED_PAYMENT: 'FETCHED_PAYMENT',
  FETCHING_PAYMENT: 'FETCHING_PAYMENT',
  FETCHING_PAYMENT_FAILED: 'FETCHING_PAYMENT_FAILED',
  FETCHED_PAYMENTS: 'FETCHED_PAYMENTS',
  FETCHING_PAYMENTS: 'FETCHING_PAYMENTS',
  FETCHING_PAYMENTS_FAILED: 'FETCHING_PAYMENTS_FAILED',
  SEARCHING_PAYMENTS: 'SEARCHING_PAYMENTS',
  SEARCHING_PAYMENTS_FAILED: 'SEARCHING_PAYMENTS_FAILED',
  SORTING_PAYMENTS: 'SORTING_PAYMENTS',
  SORTING_PAYMENTS_FAILED: 'SORTING_PAYMENTS_FAILED',
  LINKED_INVOICE_AND_PAYMENT: 'LINKED_INVOICE_AND_PAYMENT',
  LINKING_INVOICE_AND_PAYMENT_FAILED: 'LINKING_INVOICE_AND_PAYMENT_FAILED',
  LINKING_INVOICE_AND_PAYMENT: 'LINKING_INVOICE_AND_PAYMENT',
  UNLINKING_INVOICES_FOR_PAYMENT: 'UNLINKING_INVOICES_FOR_PAYMENT',
  UNLINKED_INVOICES_FOR_PAYMENT: 'UNLINKED_INVOICES_FOR_PAYMENT',
  UNLINKING_INVOICES_FOR_PAYMENT_FAILED: 'UNLINKING_INVOICES_FOR_PAYMENT_FAILED',
  UPDATING_PAYMENT_USER: 'UPDATING_PAYMENT_USER',
  UPDATED_PAYMENT_USER: 'UPDATED_PAYMENT_USER',
  UPDATING_PAYMENT_USER_FAILED: 'UPDATING_PAYMENT_USER_FAILED',
  UPDATING_PAYMENT_INCLUDE_IN_BALANCE: 'UPDATING_PAYMENT_INCLUDE_IN_BALANCE',
  UPDATED_PAYMENT_INCLUDE_IN_BALANCE: 'UPDATED_PAYMENT_INCLUDE_IN_BALANCE',
  UPDATING_PAYMENT_INCLUDE_IN_BALANCE_FAILED: 'UPDATING_PAYMENT_INCLUDE_IN_BALANCE_FAILED',
  SET_PAGE: 'SET_PAGE',
  SET_FILTER_PAYMENTS: 'SET_FILTER_PAYMENTS',
  SET_PAGESIZE_PAYMENTS: 'SET_PAGESIZE_PAYMENTS',
  SELECTING_PAYMENT: 'SELECTING_PAYMENT',
  EDITING_PAYMENT_STATUS: 'EDITING_PAYMENT_STATUS',
  CHANGE_PAYMENT_STATUS: 'CHANGE_PAYMENT_STATUS',
  CANCEL_UPDATE_PAYMENT_STATUS: 'CANCEL_UPDATE_PAYMENT_STATUS',
  UPDATING_PAYMENT_STATUS: 'UPDATING_PAYMENT_STATUS',
  UPDATING_PAYMENT_STATUS_FAILED: 'UPDATING_PAYMENT_STATUS_FAILED',
  UPDATED_PAYMENT_STATUS: 'UPDATED_PAYMENT_STATUS'
}

export const PaymentStatuses = {
  READ_ONLY: 'READ_ONLY',
  EDITING_STATUS: 'EDITING_STATUS',
  UPDATED_PAYMENT: 'UPDATED_PAYMENT',
  UPDATING_PAYMENT_STATUS: 'UPDATING_PAYMENT_STATUS',
  UPDATING_PAYMENT_STATUS_FAILED: 'UPDATING_PAYMENT_STATUS_FAILED',
  UPDATING_PAYMENT_FAILED: 'UPDATING_PAYMENT_FAILED'
}

export const paymentReducer = (state = {
  payment: {name: '', number: '', paymentId: ''},
  payments: [],
  paymentSearch: '',
  view: {
    isPaymentModalOpen: false,
    isSelectPaymentModalOpen: false,
    payment: {
      status: PaymentStatuses.READ_ONLY
    },
    payments: {
      table: {
        page: 1,
        pageSize: cookies.get('paym:ps') != null ? cookies.get('paym:ps') : 10,
        fullSize: 0,
        filter: '',
        showFilter: true,
        orderBy: cookies.get('paym:orderBy') != null ? cookies.get('paym:orderBy') : 'date',
        asc: cookies.get('paym:asc') != null ? cookies.get('paym:asc') : 0,
        showPagination: true,
        columns: [
          {label: '', sortField: 'payment_id'},
          {label: 'Gebruiker', sortField: 'name'},
          {label: 'Naam', sortField: 'name'},
          {label: 'Bedrag', sortField: 'amount'},
          {label: 'Datum', sortField: 'date'},
          {label: 'Status', sortField: 'status'},
          {label: 'afrekening', sortField: null},
          {label: 'mededeling', sortField: null}
        ]
      }
    },
    selectPayment: {
      invoiceAndUser: {},
      table: {
        page: 1,
        pageSize: 50,
        fullSize: 0,
        filter: '',
        orderBy: 'date',
        asc: 0,
        columns: [
          {label: '', sortField: null},
          {label: 'Number', sortField: 'number'},
          {label: 'Gebruiker', sortField: 'name'},
          {label: 'Naam op overschrijving', sortField: null},
          {label: 'Bedrag', sortField: 'amount'},
          {label: 'Datum', sortField: 'date'},
          {label: 'Status', sortField: 'status'},
          {label: 'mededeling', sortField: null}
        ],
        showFilter: true,
        showPagination: true
      }
    },
    userPicker: {
      paymentId: null,
      isUserPickerModalOpen: false
    }
  }
}, action) => {
  switch (action.type) {
    case 'FETCHED_PAYMENTS_SELECT_PAYMENT':
      return update(state, {
        status: {$set: PaymentsStatuses.FETCHED_PAYMENTS},
        payments: {$set: action.payments},
        view: {
          selectPayment: {
            table: {
              fullSize: {$set: action.fullSize}
            }
          }
        },
      })
    case 'SHOW_SELECT_PAYMENT_MODAL':
      return update(state, {
        view: {
          isSelectPaymentModalOpen: {$set: true}
        },
        invoices: {$set: []},
        invoiceSearch: {$set: ''}
      })
    case 'HIDE_SELECT_PAYMENT_MODAL':
      return update(state, {
        view: {
          isSelectPaymentModalOpen: {$set: false}
        }
      })
    case 'SELECTING_PAYMENT':
      return update(state, {
        status: {$set: PaymentsStatuses.SELECTING_PAYMENT},
        view: {
          selectPayment: {
            invoiceAndUser: {$set: action.invoiceAndUser}
          },
          isSelectPaymentModalOpen: {$set: true}
        }
      })
    case 'SET_PAGE_SELECT_PAYMENT':
      return update(state, {
        view: {
          selectPayment: {
            table: {
              page: {$set: action.page}
            }
          }
        }
      })
    case 'SET_SORT_ORDER_SELECT_PAYMENT':
      return update(state, {
        view: {
          selectPayment: {
            table: {
              orderBy: {$set: action.orderBy},
              asc: {$set: action.asc}
            }
          }
        }
      })
    case 'SET_FILTER_SELECT_PAYMENT':
      return update(state, {
        view: {
          selectPayment: {
            table: {
              filter: {$set: action.filter}
            }
          }
        }
      })
    case 'UNLINKED_INVOICES_FOR_PAYMENT':
      const payments1 = state.payments.map(paymentAndUser =>
        paymentAndUser.payment.paymentId === action.paymentAndUser.payment.paymentId ? action.paymentAndUser : paymentAndUser
      )
      return update(state, {
        status: {$set: PaymentsStatuses.UNLINKED_INVOICES_FOR_PAYMENT},
        payments: {$set: payments1}
      })
    case 'UNLINKING_INVOICES_FOR_PAYMENT_FAILED':
      return update(state, {
        status: {$set: PaymentsStatuses.UNLINKING_INVOICES_FOR_PAYMENT_FAILED},
      })
    case 'UNLINKING_INVOICES_FOR_PAYMENT':
      return update(state, {
        status: {$set: PaymentsStatuses.UNLINKING_INVOICES_FOR_PAYMENT},
      })

    case 'LINKED_INVOICE_AND_PAYMENT':
      return update(state, {
        view: {
          isSelectPaymentModalOpen: {$set: false}
        }
      })
    case 'LINKING_INVOICE_AND_PAYMENT_FAILED':
      return update(state, {
        status: {$set: PaymentsStatuses.LINKING_INVOICE_AND_PAYMENT_FAILED},
      })
    case 'LINKING_INVOICE_AND_PAYMENT':
      return update(state, {
        status: {$set: PaymentsStatuses.LINKING_INVOICE_AND_PAYMENT},
      })
    case 'FETCHED_PAYMENT':
      return update(state, {
          payment: {$set: action.payment},
          view : {
            payment: {
              status: {$set: PaymentsStatuses.FETCHED_PAYMENT}
            }
          }
        })
    case 'FETCHING_PAYMENT':
      return update(state, {
          status: {$set: PaymentsStatuses.FETCHING_PAYMENT},
        })
    case 'FETCHING_PAYMENT_FAILED':
      return update(state, {
        status: {$set: PaymentsStatuses.FETCHING_PAYMENT_FAILED},
      })
    case 'FETCHED_PAYMENTS':
      return update(state, {
          payments: {$set: action.payments},
          view: {
            payments: {
              table: {
                fullSize: {$set: action.fullSize}
              }
            }
          },
          status: {$set: PaymentsStatuses.FETCHED_PAYMENTS},
        })
    case 'FETCHING_PAYMENTS':
      return update(state, {
          status: {$set: PaymentsStatuses.FETCHING_PAYMENTS},
        })
    case 'FETCHING_PAYMENTS_FAILED':
      return update(state, {
        status: {$set: PaymentsStatuses.FETCHING_PAYMENTS_FAILED},
      })
    case 'SEARCHING_PAYMENTS':
      return update(state, {
        status: {$set: PaymentsStatuses.SEARCHING_PAYMENTS},
        paymentSearch: {$set: action.paymentSearch}
      })
    case 'SEARCHING_PAYMENTS_FAILED':
      return update(state, {
        status: {$set: PaymentsStatuses.SEARCHING_PAYMENTS_FAILED}
      })
    case 'SET_PAGE_PAYMENTS':
      return update(state, {
        view: {
          payments: {
            table: {
              page: {$set: action.page}
            }
          }
        }
      })
    case 'SET_SORT_ORDER_PAYMENTS':
      return update(state, {
        view: {
          payments: {
            table: {
              orderBy: {$set: action.orderBy},
              asc: {$set: action.asc}
            }
          }
        }
      })
    case 'SET_FILTER_PAYMENTS':
      return update(state, {
        view: {
          payments: {
            table: {
              filter: {$set: action.filter},
              page: {$set: '1'}
            }
          }
        }
      })
    case 'SET_PAGESIZE_PAYMENTS':
      return update(state, {
        view: {
          payments: {
            table: {
              pageSize: {$set: action.pageSize}
            }
          }
        }
      })
    case 'UPDATED_PAYMENT_USER':
    case 'UPDATED_PAYMENT_INCLUDE_IN_BALANCE':
    case 'UPDATED_PAYMENT_STATUS':
      const payments2 = state.payments.map(paymentAndUser =>
        paymentAndUser.payment.paymentId === action.paymentAndUser.payment.paymentId ? action.paymentAndUser : paymentAndUser
      )
      return update(state, {
        payments: {$set: payments2},
        view: {
          payment: {
            status: {$set: PaymentStatuses.UPDATED_PAYMENT},
          },
          userPicker: {
            isUserPickerModalOpen: {$set: false}
          }
        }
      })
    case 'UPDATING_PAYMENT_USER_FAILED':
    case 'UPDATING_PAYMENT_INCLUDE_IN_BALANCE_FAILED':
    case 'UPDATING_PAYMENT_STATUS_FAILED':
      return update(state, {
        view: {
          payment: {
            status: {$set: PaymentStatuses.UPDATING_PAYMENT_FAILED},
          }
        }
      })
    case 'UPDATING_PAYMENT_USER':
    case 'UPDATING_PAYMENT_STATUS':
    case 'UPDATING_PAYMENT_INCLUDE_IN_BALANCE':
      return update(state, {
        view: {
          payment: {
            status: {$set: PaymentStatuses.UPDATING_PAYMENT},
          }
        }
      })
    case 'SHOW_USER_PICKER':
      return update(state, {
        view: {
          userPicker: {
            paymentId: {$set: action.paymentId},
            isUserPickerModalOpen: {$set: true}
          }
        }
      })
    case 'HIDE_USER_PICKER':
      return update(state, {
        view: {
          userPicker: {
            paymentId: {$set: action.paymentId},
            isUserPickerModalOpen: {$set: false}
          }
        }
      })
    case 'SHOW_PAYMENT':
      return update(state, {
        view: {
          isPaymentModalOpen: {$set: true}
        }
      })
    case 'HIDE_PAYMENT':
      return update(state, {
        view: {
          isPaymentModalOpen: {$set: false}
        }
      })
    case 'EDITING_PAYMENT_STATUS':
      return update(state, {
        view: {
          payment: {
            paymentId: {$set: action.id},
            status: {$set: PaymentStatuses.EDITING_STATUS},
            tempStatus: {$set: action.tempStatus}
          }
        }
      })
    case 'CHANGE_PAYMENT_STATUS':
      return update(state, {
        view: {
          payment: {
            tempStatus: {$set: action.tempStatus}
          }
        }
      })
    case 'CANCEL_UPDATE_PAYMENT_STATUS':
      return update(state, {
        view: {
          payment: {
            status: {$set: PaymentStatuses.READ_ONLY}
          }
        }
      })
    default:
      return state;
  }
};
