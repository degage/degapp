/* Reports.java
 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 * Copyright Ⓒ 2014-2015 Universiteit Gent
 * 
 * This file is part of the Degage Web Application
 * 
 * Corresponding author (see also AUTHORS.txt)
 * 
 * Kris Coolsaet
 * Department of Applied Mathematics, Computer Science and Statistics
 * Ghent University 
 * Krijgslaan 281-S9
 * B-9000 GENT Belgium
 * 
 * The Degage Web Application is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The Degage Web Application is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with the Degage Web Application (file LICENSE.txt in the
 * distribution).  If not, see http://www.gnu.org/licenses/.
 */

package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.CarRideDAO;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;
import com.google.common.base.Strings;
import controllers.util.Pagination;
import db.CurrentUser;
import db.DataAccess;
import db.InjectContext;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 15/05/14.
 */
public class Reports extends Controller {

    @InjectContext
    public static Result index() {
        return ok(reports.render());
    }

    @AllowRoles({UserRole.SUPER_USER})
    @InjectContext
    public static Result getUsers() {
        File file = new File("users.xlsx");
        UserDAO userDao = DataAccess.getInjectedContext().getUserDAO();
        List<User> userList = userDao.getUserList(FilterField.USER_NAME, true, 1, userDao.getAmountOfUsers(null), null);
        try (FileOutputStream out = new FileOutputStream(file)) {
            Workbook wb = new XSSFWorkbook();
            Sheet s = wb.createSheet("Gebruikers");
            int rNum = 0;
            Row row = s.createRow(rNum);
            String[] header = {"Id", "Voornaam", "Familienaam", "Email", "Telefoon", "Gsm", "Straat (domicilie)",
                    "Huisnummer (domicilie)", "Postcode (domicilie)", "Stad (domicilie)", "Land (domicilie)", "Straat (verblijf)",
                    "Huisnummer (Verblijf)", "Postcode (verblijf)", "Stad (verblijf)", "Land (verblijf)", "Geslacht", "Rijbewijs",
                    "Gebruikersstatus", "Identiteitskaart", "Schadeverleden"};
            for (int i = 0; i < header.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(header[i]);
            }
            rNum++;
            User user = null;
            for (int i = 0; i < userList.size(); i++) {
                user = userList.get(i);
                row = s.createRow(i + 1);
                int j = 0;
                row.createCell(j++).setCellValue(user.getId());
                row.createCell(j++).setCellValue(user.getFirstName());
                row.createCell(j++).setCellValue(user.getLastName());
                row.createCell(j++).setCellValue(user.getEmail());
                row.createCell(j++).setCellValue(user.getPhone());
                row.createCell(j++).setCellValue(user.getCellphone());

                row.createCell(j++).setCellValue(user.getAddressDomicile().getStreet());
                row.createCell(j++).setCellValue(user.getAddressDomicile().getNum());
                row.createCell(j++).setCellValue(user.getAddressDomicile().getZip());
                row.createCell(j++).setCellValue(user.getAddressDomicile().getCity());
                row.createCell(j++).setCellValue(user.getAddressDomicile().getCountry());

                row.createCell(j++).setCellValue(user.getAddressResidence().getStreet());
                row.createCell(j++).setCellValue(user.getAddressResidence().getNum());
                row.createCell(j++).setCellValue(user.getAddressResidence().getZip());
                row.createCell(j++).setCellValue(user.getAddressResidence().getCity());
                row.createCell(j++).setCellValue(user.getAddressResidence().getCountry());

                row.createCell(j++).setCellValue(user.getGender().name());
                row.createCell(j++).setCellValue(Strings.nullToEmpty(user.getLicense()));
                row.createCell(j++).setCellValue(user.getStatus().name());
                row.createCell(j++).setCellValue(Strings.nullToEmpty(user.getNationalId()));
                row.createCell(j++).setCellValue(user.getDamageHistory());
            }
            wb.write(out);
            return ok(file, file.getName());
        } catch (IOException ex) {
            throw new RuntimeException(ex); // TODO
        }
    }

    @AllowRoles({UserRole.CAR_OWNER})
    @InjectContext
    public static Result getReservationsForOwner() {
        // TODO: factor out code in common with getReservations
        Filter filter = Pagination.parseFilter("");
        filter.putValue(FilterField.RESERVATION_USER_OR_OWNER_ID, CurrentUser.getId());
        File file = new File("reservations.xlsx");
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO reservationDAO = context.getReservationDAO();
        CarRideDAO carRideDAO = context.getCarRideDAO();
        Iterable<Reservation> reservationList = reservationDAO.getReservationListPage(FilterField.FROM, true, 1, reservationDAO.getAmountOfReservations(filter), filter);
        try (FileOutputStream out = new FileOutputStream(file)) {
            Workbook wb = new XSSFWorkbook();
            CreationHelper createHelper = wb.getCreationHelper();
            Sheet s = wb.createSheet("Reservaties");
            Row row = s.createRow(0);
            String[] header = {"Id", "Autonaam", "Lener(ID)", "Lener voornaam", "Lener familienaam", "Lener email", "Lener telefoon", "Lener gsm", "Van", "Tot", "Status", "Bericht", "Startkilometers", "Eindkilometers", "Schade", "Details goedgekeurd"};
            for (int i = 0; i < header.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(header[i]);
            }
            int i = 0;
            for (Reservation reservation : reservationList) {
                i ++;
                row = s.createRow(i);
                int j = 0;
                UserHeader user = reservation.getUser();
                row.createCell(j++).setCellValue(reservation.getId());
                row.createCell(j++).setCellValue(reservation.getCar().getName());
                row.createCell(j++).setCellValue(user.getId());
                row.createCell(j++).setCellValue(user.getFirstName());
                row.createCell(j++).setCellValue(user.getLastName());
                row.createCell(j++).setCellValue(user.getEmail());
                row.createCell(j++).setCellValue(user.getPhone());
                row.createCell(j++).setCellValue(user.getCellPhone());
                Cell cell;
                CellStyle cellStyle = wb.createCellStyle();
                cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
                cell = row.createCell(j++);
                cell.setCellValue(Date.from(Instant.from(reservation.getFrom())));
                cell.setCellStyle(cellStyle);
                cell = row.createCell(j++);
                cell.setCellValue(Date.from(Instant.from(reservation.getUntil())));
                cell.setCellStyle(cellStyle);
                row.createCell(j++).setCellValue(reservation.getStatus().toString());
                row.createCell(j++).setCellValue(reservation.getMessage());
                if (reservation.getStatus() == ReservationStatus.DETAILS_PROVIDED) {
                    CarRide carRide = carRideDAO.getCarRide(reservation.getId());
                    row.createCell(j++).setCellValue(carRide.getStartKm());
                    row.createCell(j++).setCellValue(carRide.getEndKm());
                    row.createCell(j++).setCellValue(carRide.isDamaged());
                    //row.createCell(j++).setCellValue(carRide.isApprovedByOwner());
                }
            }
            wb.write(out);
            return ok(file, file.getName());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }


    }

    @AllowRoles({UserRole.SUPER_USER, UserRole.RESERVATION_ADMIN})
    @InjectContext
    public static Result getReservations() {
        File file = new File("reservations.xlsx");
        DataAccessContext context = DataAccess.getInjectedContext();
        ReservationDAO reservationDAO = context.getReservationDAO();
        CarRideDAO carRideDAO = context.getCarRideDAO();
        Filter filter = Pagination.parseFilter("");
        filter.putValue(FilterField.RESERVATION_USER_OR_OWNER_ID, "");
        filter.putValue(FilterField.RESERVATION_CAR_ID, "");
        Iterable<Reservation> reservationList = reservationDAO.getReservationListPage(FilterField.RESERVATION_CAR_ID, true, 1, reservationDAO.getAmountOfReservations(filter), filter);
        try (FileOutputStream out = new FileOutputStream(file)) {
            Workbook wb = new XSSFWorkbook();
            CreationHelper createHelper = wb.getCreationHelper();
            Sheet s = wb.createSheet("Reservaties");
            Row row = s.createRow(0);
            String[] header = {"Id", "Auto(ID)", "Autonaam", "Lener(ID)", "Lener voornaam", "Lener familienaam", "Lener email", "Lener telefoon", "Lener gsm", "Van", "Tot", "Status", "Bericht", "Startkilometers", "Eindkilometers", "Schade", "Details goedgekeurd"};
            for (int i = 0; i < header.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(header[i]);
            }
            int i=0;
            for (Reservation reservation : reservationList){
                i ++;
                row = s.createRow(i);
                int j = 0;
                row.createCell(j++).setCellValue(reservation.getId());
                CarHeader car = reservation.getCar();
                UserHeader user = reservation.getUser();
                row.createCell(j++).setCellValue(car.getId());
                row.createCell(j++).setCellValue(car.getName());
                row.createCell(j++).setCellValue(user.getId());
                row.createCell(j++).setCellValue(user.getFirstName());
                row.createCell(j++).setCellValue(user.getLastName());
                row.createCell(j++).setCellValue(user.getEmail());
                row.createCell(j++).setCellValue(user.getPhone());
                row.createCell(j++).setCellValue(user.getCellPhone());
                Cell cell;
                CellStyle cellStyle = wb.createCellStyle();
                cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
                cell = row.createCell(j++);
                cell.setCellValue(Date.from(Instant.from(reservation.getFrom())));
                cell.setCellStyle(cellStyle);
                cell = row.createCell(j++);
                cell.setCellValue(Date.from(Instant.from(reservation.getUntil())));
                cell.setCellStyle(cellStyle);
                row.createCell(j++).setCellValue(reservation.getStatus().toString());
                row.createCell(j++).setCellValue(reservation.getMessage());
                if (reservation.getStatus() == ReservationStatus.DETAILS_PROVIDED) {
                    CarRide carRide = carRideDAO.getCarRide(reservation.getId());
                    row.createCell(j++).setCellValue(carRide.getStartKm());
                    row.createCell(j++).setCellValue(carRide.getEndKm());
                    row.createCell(j++).setCellValue(carRide.isDamaged());
                    //row.createCell(j++).setCellValue(carRide.isApprovedByOwner());
                }
            }
            wb.write(out);
            return ok(file, file.getName());

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    @AllowRoles({UserRole.SUPER_USER, UserRole.CAR_ADMIN})
    @InjectContext
    public static Result getCars() {
        File file = new File("cars.xlsx");
        Iterable<Car> carList = DataAccess.getInjectedContext().getCarDAO().listAllCars();
        try (FileOutputStream out = new FileOutputStream(file)) {
            Workbook wb = new XSSFWorkbook();
            CreationHelper createHelper = wb.getCreationHelper();
            Sheet s = wb.createSheet("Auto's");

            // header
            Row row = s.createRow(0);
            String[] header = {"Id", "Naam", "Merk", "Type", "Straat (locatie)", "Huisnummer (locatie)",
                    "Postcode (locatie)", "Stad (locatie)", "Plaatsen", "Deuren", "Bouwjaar", "Manueel", "Gps",
                    "Trekhaak", "Brandstof", "Verbuik (l/100km)", "Geschatte marktprijs",
                    "Jaarlijkse kilometers door eigenaar", "Nummerplaat", "Chassisnr",
                    "Verzekering", "Verzekeringspolis", "Verzekeringsafloop", "Bonus-malus", "Eigenaar (ID)",
                    "Commentaar", "Actief"};
            for (int i = 0; i < header.length; i++) {
                Cell cell = row.createCell(i);
                cell.setCellValue(header[i]);
            }

            // contents
            int rowNumber = 1;
            for (Car car : carList) {
                row = s.createRow(rowNumber);

                int j = 0;
                row.createCell(j++).setCellValue(car.getId());
                row.createCell(j++).setCellValue(car.getName());
                row.createCell(j++).setCellValue(car.getBrand());
                row.createCell(j++).setCellValue(car.getType());

                row.createCell(j++).setCellValue(car.getLocation().getStreet());
                row.createCell(j++).setCellValue(car.getLocation().getNum());
                row.createCell(j++).setCellValue(car.getLocation().getZip());
                row.createCell(j++).setCellValue(car.getLocation().getCity());

                row.createCell(j++).setCellValue(car.getSeats());
                row.createCell(j++).setCellValue(car.getDoors());
                row.createCell(j++).setCellValue(checkNotNullOrZero(car.getYear()).toString());
                row.createCell(j++).setCellValue(car.isManual());
                row.createCell(j++).setCellValue(car.isGps());
                row.createCell(j++).setCellValue(car.isHook());
                row.createCell(j++).setCellValue(car.getFuel().toString());
                row.createCell(j++).setCellValue(checkNotNullOrZero(car.getFuelEconomy()).toString());
                row.createCell(j++).setCellValue(checkNotNullOrZero(car.getEstimatedValue()).toString());
                row.createCell(j++).setCellValue(checkNotNullOrZero(car.getOwnerAnnualKm()).toString());

                row.createCell(j++).setCellValue(car.getTechnicalCarDetails().getLicensePlate());
                row.createCell(j++).setCellValue(checkNotNullOrZero(car.getTechnicalCarDetails().getChassisNumber()).toString());

                row.createCell(j++).setCellValue(car.getInsurance().getName());
                row.createCell(j++).setCellValue(car.getInsurance().getPolisNr());

                // format as date

                CellStyle cellStyle = wb.createCellStyle();
                cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
                Cell cell = row.createCell(j++);
                LocalDate expiration = car.getInsurance().getExpiration();
                if (expiration != null) {
                    cell.setCellValue(
                            Date.from (Instant.from(expiration))
                    );
                } else {
                    cell.setCellValue("");
                }
                cell.setCellStyle(cellStyle);

                row.createCell(j++).setCellValue(checkNotNullOrZero(car.getInsurance().getBonusMalus()).toString());

                row.createCell(j++).setCellValue(car.getOwner().getId());
                row.createCell(j++).setCellValue(car.getComments());
                row.createCell(j++).setCellValue(car.isActive());

                rowNumber++;
            }
            wb.write(out);
            return ok(file, file.getName());

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Object checkNotNullOrZero(Object object) {
        if (object != null) {
            return object;
        } else {
            return "";
        }
    }
}
