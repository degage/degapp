package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationStatus;
import notifiers.Notifier;

/**
 * Job that automatically accepts reservations that are out of date
 */
public class ReservationAutoAcceptJob implements ScheduledJob.Executor {
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
