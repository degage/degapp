package controllers;

import be.ugent.degage.db.DataAccessContext;
import be.ugent.degage.db.DataAccessException;
import be.ugent.degage.db.Filter;
import be.ugent.degage.db.FilterField;
import be.ugent.degage.db.dao.CarDAO;
import be.ugent.degage.db.dao.CarRideDAO;
import be.ugent.degage.db.dao.ReservationDAO;
import be.ugent.degage.db.dao.UserDAO;
import be.ugent.degage.db.models.*;
import controllers.Security.RoleSecured;
import controllers.util.Pagination;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import play.mvc.Controller;
import play.mvc.Result;
import providers.DataProvider;
import views.html.reports;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Stefaan Vermassen on 15/05/14.
 */
public class Reports extends Controller {

    public static Result index() {
        return ok(reports.render());
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER})
    public static Result getUsers()  {
        File file = new File("users.xlsx");
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            UserDAO userDao = context.getUserDAO();
            List<User> userList = userDao.getUserList(FilterField.USER_NAME, true, 1, userDao.getAmountOfUsers(null), null);
            try(FileOutputStream out = new FileOutputStream(file)){
                Workbook wb = new XSSFWorkbook();
                Sheet s = wb.createSheet("Gebruikers");
                int rNum = 0;
                Row row = s.createRow(rNum);
                String[] header = {"Id", "Voornaam", "Familienaam", "Email", "Telefoon", "Gsm", "Straat (domicilie)",
                        "Huisnummer (domicilie)", "Postcode (domicilie)", "Stad (domicilie)", "Land (domicilie)", "Straat (verblijf)",
                        "Huisnummer (Verblijf)", "Postcode (verblijf)", "Stad (verblijf)", "Land (verblijf)", "Geslacht", "Rijbewijs",
                        "Gebruikersstatus", "Identiteitskaart", "Schadeverleden"};
                for(int i=0; i<header.length; i++){
                    Cell cell = row.createCell(i);
                    cell.setCellValue(header[i]);
                }
                rNum++;
                User user = null;
                for(int i=0; i<userList.size(); i++){
                    user = userList.get(i);
                    row = s.createRow(i+1);
                    int j=0;
                    row.createCell(j++).setCellValue(user.getId());
                    row.createCell(j++).setCellValue(user.getFirstName());
                    row.createCell(j++).setCellValue(user.getLastName());
                    row.createCell(j++).setCellValue(user.getEmail());
                    row.createCell(j++).setCellValue(user.getPhone());
                    row.createCell(j++).setCellValue(user.getCellphone());
                    if(user.getAddressDomicile() != null){
                        row.createCell(j++).setCellValue(user.getAddressDomicile().getStreet());
                        row.createCell(j++).setCellValue(user.getAddressDomicile().getNumber());
                        row.createCell(j++).setCellValue(user.getAddressDomicile().getZip());
                        row.createCell(j++).setCellValue(user.getAddressDomicile().getCity());
                        row.createCell(j++).setCellValue(user.getAddressDomicile().getCountry());
                    }else{
                        j+=5;
                    }
                    if(user.getAddressResidence() != null){
                        row.createCell(j++).setCellValue(user.getAddressResidence().getStreet());
                        row.createCell(j++).setCellValue(user.getAddressResidence().getNumber());
                        row.createCell(j++).setCellValue(user.getAddressResidence().getZip());
                        row.createCell(j++).setCellValue(user.getAddressResidence().getCity());
                        row.createCell(j++).setCellValue(user.getAddressResidence().getCountry());
                    }else{
                        j+=5;
                    }
                    row.createCell(j++).setCellValue(user.getGender().name());
                    row.createCell(j++).setCellValue((user.getDriverLicense()!= null)? user.getDriverLicense().getId(): "");
                    row.createCell(j++).setCellValue(user.getStatus().name());
                    row.createCell(j++).setCellValue((user.getIdentityCard() != null)?user.getIdentityCard().getRegistrationNr(): "");
                    row.createCell(j++).setCellValue(user.getDamageHistory());
                }
                wb.write(out);
                return ok(file, file.getName());
            }
        } catch (DataAccessException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.CAR_OWNER})
    public static Result getReservationsForOwner(){
        User user = DataProvider.getUserProvider().getUser();
        Filter filter = Pagination.parseFilter("");
        filter.putValue(FilterField.RESERVATION_USER_OR_OWNER_ID, "" + user.getId());
        File file = new File("reservations.xlsx");
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            ReservationDAO reservationDAO = context.getReservationDAO();
            CarRideDAO carRideDAO = context.getCarRideDAO();
            List<Reservation> reservationList = reservationDAO.getReservationListPage(FilterField.FROM, true, 1, reservationDAO.getAmountOfReservations(filter), filter);
            try(FileOutputStream out = new FileOutputStream(file)){
                Workbook wb = new XSSFWorkbook();
                CreationHelper createHelper = wb.getCreationHelper();
                Sheet s = wb.createSheet("Reservaties");
                int rNum = 0;
                Row row = s.createRow(rNum);
                String[] header = {"Id", "Autonaam", "Lener(ID)", "Lener voornaam","Lener familienaam", "Lener email", "Lener telefoon", "Lener gsm", "Van", "Tot", "Status", "Bericht", "Startkilometers", "Eindkilometers", "Schade", "Details goedgekeurd"};
                for(int i=0; i<header.length; i++){
                    Cell cell = row.createCell(i);
                    cell.setCellValue(header[i]);
                }
                Reservation reservation = null;
                CarRide carRide = null;
                for(int i=0; i<reservationList.size(); i++) {
                    reservation = reservationList.get(i);
                    row = s.createRow(i + 1);
                    int j = 0;
                    row.createCell(j++).setCellValue(reservation.getId());
                    row.createCell(j++).setCellValue(reservation.getCar().getName());
                    row.createCell(j++).setCellValue(reservation.getUser().getId());
                    row.createCell(j++).setCellValue(reservation.getUser().getFirstName());
                    row.createCell(j++).setCellValue(reservation.getUser().getLastName());
                    row.createCell(j++).setCellValue(reservation.getUser().getEmail());
                    row.createCell(j++).setCellValue(reservation.getUser().getPhone());
                    row.createCell(j++).setCellValue(reservation.getUser().getCellphone());
                    Cell cell;
                    CellStyle cellStyle = wb.createCellStyle();
                    cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
                    cell = row.createCell(j++);
                    cell.setCellValue(reservation.getFrom().toDate());
                    cell.setCellStyle(cellStyle);
                    cell = row.createCell(j++);
                    cell.setCellValue(reservation.getTo().toDate());
                    cell.setCellStyle(cellStyle);
                    row.createCell(j++).setCellValue(reservation.getStatus().getDescription());
                    row.createCell(j++).setCellValue(reservation.getMessage());
                    if(reservation.getStatus() == ReservationStatus.DETAILS_PROVIDED){
                        carRide = carRideDAO.getCarRide(reservation.getId());
                        row.createCell(j++).setCellValue(carRide.getStartMileage());
                        row.createCell(j++).setCellValue(carRide.getEndMileage());
                        row.createCell(j++).setCellValue(carRide.isDamaged());
                        row.createCell(j++).setCellValue(carRide.isStatus());
                    }
                }
                wb.write(out);
                return ok(file, file.getName());
            }
        }catch (DataAccessException | IOException ex) {
            throw new RuntimeException(ex);
        }


    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER, UserRole.RESERVATION_ADMIN})
    public static Result getReservations(){
        File file = new File("reservations.xlsx");
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
           ReservationDAO reservationDAO = context.getReservationDAO();
            CarRideDAO carRideDAO = context.getCarRideDAO();
            Filter filter = Pagination.parseFilter("");
            filter.putValue(FilterField.RESERVATION_USER_OR_OWNER_ID, "");
            filter.putValue(FilterField.RESERVATION_CAR_ID, "");
           List<Reservation> reservationList = reservationDAO.getReservationListPage(FilterField.RESERVATION_CAR_ID, true, 1, reservationDAO.getAmountOfReservations(filter), filter);
           try(FileOutputStream out = new FileOutputStream(file)){
               Workbook wb = new XSSFWorkbook();
               CreationHelper createHelper = wb.getCreationHelper();
               Sheet s = wb.createSheet("Reservaties");
               int rNum = 0;
               Row row = s.createRow(rNum);
               String[] header = {"Id", "Auto(ID)", "Autonaam", "Lener(ID)", "Lener voornaam","Lener familienaam", "Lener email", "Lener telefoon", "Lener gsm", "Van", "Tot", "Status", "Bericht", "Startkilometers", "Eindkilometers", "Schade", "Details goedgekeurd"};
               for(int i=0; i<header.length; i++){
                   Cell cell = row.createCell(i);
                   cell.setCellValue(header[i]);
               }
               rNum++;
               Reservation reservation = null;
               CarRide carRide = null;
               for(int i=0; i<reservationList.size(); i++){
                   reservation = reservationList.get(i);
                   row = s.createRow(i+1);
                   int j=0;
                   row.createCell(j++).setCellValue(reservation.getId());
                   row.createCell(j++).setCellValue(reservation.getCar().getId());
                   row.createCell(j++).setCellValue(reservation.getCar().getName());
                   row.createCell(j++).setCellValue(reservation.getUser().getId());
                   row.createCell(j++).setCellValue(reservation.getUser().getFirstName());
                   row.createCell(j++).setCellValue(reservation.getUser().getLastName());
                   row.createCell(j++).setCellValue(reservation.getUser().getEmail());
                   row.createCell(j++).setCellValue(reservation.getUser().getPhone());
                   row.createCell(j++).setCellValue(reservation.getUser().getCellphone());
                   Cell cell;
                   CellStyle cellStyle = wb.createCellStyle();
                   cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));
                   cell = row.createCell(j++);
                   cell.setCellValue(reservation.getFrom().toDate());
                   cell.setCellStyle(cellStyle);
                   cell = row.createCell(j++);
                   cell.setCellValue(reservation.getTo().toDate());
                   cell.setCellStyle(cellStyle);
                   row.createCell(j++).setCellValue(reservation.getStatus().getDescription());
                   row.createCell(j++).setCellValue(reservation.getMessage());
                   if(reservation.getStatus() == ReservationStatus.DETAILS_PROVIDED){
                       carRide = carRideDAO.getCarRide(reservation.getId());
                       row.createCell(j++).setCellValue(carRide.getStartMileage());
                       row.createCell(j++).setCellValue(carRide.getEndMileage());
                       row.createCell(j++).setCellValue(carRide.isDamaged());
                       row.createCell(j++).setCellValue(carRide.isStatus());
                   }
               }
               wb.write(out);
               return ok(file, file.getName());
           }
        } catch (DataAccessException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @RoleSecured.RoleAuthenticated({UserRole.SUPER_USER, UserRole.CAR_ADMIN})
    public static Result getCars(){
        File file = new File("cars.xlsx");
        try (DataAccessContext context = DataProvider.getDataAccessProvider().getDataAccessContext()) {
            CarDAO carDAO = context.getCarDAO();
            Filter filter = Pagination.parseFilter("");
            List<Car> carList = carDAO.getCarList(FilterField.CAR_NAME, true, 1, carDAO.getAmountOfCars(filter), filter);
            try(FileOutputStream out = new FileOutputStream(file)){
                Workbook wb = new XSSFWorkbook();
                CreationHelper createHelper = wb.getCreationHelper();
                Sheet s = wb.createSheet("Gebruikers");
                int rNum = 0;
                Row row = s.createRow(rNum);
                String[] header = {"Id", "Naam", "Merk", "Type", "Straat (locatie)", "Huisnummer (locatie)",
                        "Postcode (locatie)", "Stad (locatie)", "Plaatsen", "Deuren", "Bouwjaar", "Manueel", "Gps",
                        "Trekhaak", "Brandstof", "Verbuik (l/100km)", "Geschatte marktprijs",
                        "Jaarlijkse kilometers door eigenaar", "Nummerplaat",  "Chassisnr",
                        "Verzekering", "Verzekeringspolis", "Verzekeringsafloop", "Bonus-malus", "Eigenaar (ID)",
                        "Commentaar", "Actief"};
                for(int i=0; i<header.length; i++){
                    Cell cell = row.createCell(i);
                    cell.setCellValue(header[i]);
                }
                rNum++;
                Car car = null;
                for(int i=0; i<carList.size(); i++) {
                    car = carList.get(i);
                    row = s.createRow(i + 1);
                    int j = 0;
                    row.createCell(j++).setCellValue(car.getId());
                    row.createCell(j++).setCellValue(car.getName());
                    row.createCell(j++).setCellValue(car.getBrand());
                    row.createCell(j++).setCellValue(car.getType());
                    if(car.getLocation() != null){
                        row.createCell(j++).setCellValue(car.getLocation().getStreet());
                        row.createCell(j++).setCellValue(car.getLocation().getNumber());
                        row.createCell(j++).setCellValue(car.getLocation().getZip());
                        row.createCell(j++).setCellValue(car.getLocation().getCity());
                    }else{
                        j+=4;
                    }

                    row.createCell(j++).setCellValue(car.getSeats());
                    row.createCell(j++).setCellValue(car.getDoors());
                    row.createCell(j++).setCellValue(checkNotNullOrZero(car.getYear()).toString());
                    row.createCell(j++).setCellValue(car.isManual());
                    row.createCell(j++).setCellValue(car.isGps());
                    row.createCell(j++).setCellValue(car.isHook());
                    row.createCell(j++).setCellValue(car.getFuel().getDescription());
                    row.createCell(j++).setCellValue(checkNotNullOrZero(car.getFuelEconomy()).toString());
                    row.createCell(j++).setCellValue(checkNotNullOrZero(car.getEstimatedValue()).toString());
                    row.createCell(j++).setCellValue(checkNotNullOrZero(car.getOwnerAnnualKm()).toString());
                    if(car.getTechnicalCarDetails() != null){
                        row.createCell(j++).setCellValue(car.getTechnicalCarDetails().getLicensePlate());
                        row.createCell(j++).setCellValue(checkNotNullOrZero(car.getTechnicalCarDetails().getChassisNumber()).toString());
                    }else{
                        j+=2;
                    }
                    if(car.getInsurance() != null){
                        row.createCell(j++).setCellValue(car.getInsurance().getName());
                        row.createCell(j++).setCellValue(checkNotNullOrZero(car.getInsurance().getPolisNr()).toString());
                        Cell cell;
                        CellStyle cellStyle = wb.createCellStyle();
                        cellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/mm/yyyy"));
                        cell = row.createCell(j++);
                        cell.setCellValue(car.getInsurance().getExpiration());
                        cell.setCellStyle(cellStyle);
                        row.createCell(j++).setCellValue(checkNotNullOrZero(car.getInsurance().getBonusMalus()).toString());
                    }else{
                        j+=4;
                    }
                    row.createCell(j++).setCellValue(car.getOwner().getId());
                    row.createCell(j++).setCellValue(car.getComments());
                    row.createCell(j++).setCellValue(car.isActive());
                }
                wb.write(out);
                return ok(file, file.getName());
            }
        } catch (DataAccessException | IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Object checkNotNullOrZero(Object object){
        if(object != null){
            return object;
        }else{
            return "";
        }
    }
}
