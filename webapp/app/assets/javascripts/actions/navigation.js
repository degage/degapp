import { Routes } from './../reducers/navigation'
import { fetchCarApprovalsByStatus } from './../actions/car-approval'

export const SELECT_ROUTE = 'SELECT_ROUTE'

const selectingRoute = (route) => ({
    type: SELECT_ROUTE,
    route,
    receivedAt: Date.now()
})

export const selectRoute = (route) =>
  dispatch => {
    switch (route) {
        case Routes.CAR_APPROVAL_REQUEST :
            dispatch(fetchCarApprovalsByStatus('REQUEST'))
            break
        case Routes.CAR_APPROVAL_ACCEPTED:
            dispatch(fetchCarApprovalsByStatus('ACCEPTED'))
            break
        case Routes.CAR_APPROVAL_REFUSED:
            dispatch(fetchCarApprovalsByStatus('REFUSED'))
            break
    }
    dispatch(selectingRoute(route))
}
