package schedulers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.models.Job;
import be.ugent.degage.db.models.Reservation;
import be.ugent.degage.db.models.ReservationStatus;
import notifiers.Notifier;

import java.time.LocalDateTime;

/**
 * Job that automatically accepts reservations that are out of date
 */
public class ReservationAutoAcceptJob implements ScheduledJob.Executor {
    @Override
    public void execute(DataAccessContext context, Job job) {
        ReservationDAO dao = context.getReservationDAO();
        Integer reservationId = job.getRefId();
        Reservation reservation = dao.getReservation(reservationId);
        if (reservation == null) {
            return;
        }

        if (reservation.getStatus() == ReservationStatus.REQUEST) {
            if (reservation.getFrom().isBefore(LocalDateTime.now())) {
                dao.updateReservationStatus(reservationId, ReservationStatus.ACCEPTED);
                Notifier.sendReservationApprovedByOwnerMail(context, "Automatisch goedgekeurd door systeem.", reservation);
            } else {
                dao.updateReservationStatus(reservationId, ReservationStatus.CANCELLED);
            }
        }

    }
}
