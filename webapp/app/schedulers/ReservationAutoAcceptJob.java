package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationStatus;
import notifiers.Notifier;

/**
 * Created by Cedric on 5/7/2014.
 */
public class ReservationAutoAcceptJob implements ScheduledJobExecutor {
    @Override
    public void execute(DataAccessContext context, Job job) {
        ReservationDAO dao = context.getReservationDAO();
        Reservation reservation = dao.getReservation(job.getRefId());
        if (reservation == null) {
            return;
        }

        if (reservation.getStatus() == ReservationStatus.REQUEST) {
            if (reservation.getFrom().isBeforeNow()) {
                reservation.setStatus(ReservationStatus.ACCEPTED);
                Notifier.sendReservationApprovedByOwnerMail(reservation.getUser(), "Automatisch goedgekeurd door systeem.", reservation);
            } else {
                reservation.setStatus(ReservationStatus.CANCELLED);
            }
            dao.updateReservation(reservation);
        }

    }
}
