/*
 * abhishek360
 */

package org.wells.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.wells.exceptionHandler.EntityNotFoundException;
import org.wells.exceptionHandler.GenericException;
import org.wells.models.Sector;
import org.wells.repositories.CompanyRepository;
import org.wells.repositories.SectorRepository;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SectorServiceImpl implements SectorService {
    @Autowired
    SectorRepository sectorRepository;
    @Autowired
    CompanyRepository companyRepository;

    @Override
    public Sector createSector(String name, String brief) {
        Sector res;
        try{
            Sector sector = new Sector();
            sector.setSectorName(name);
            sector.setBrief(brief);
            res = sectorRepository.save(sector);
        }
        catch (NullPointerException e){
            Map<String, Object> params = new HashMap<>();
            params.put("sectorName", name);
            params.put("brief", brief);
            throw new EntityNotFoundException(Sector.class, params);
        }
        catch (Exception e){
            System.out.println(e.toString());
            throw new GenericException(e);
        }

        return res;
    }

    @Override
    public Sector updateSector(String id, String name, String brief) {
        int sectorId;
        Sector res;

        try{
            sectorId = Integer.parseInt(id);
            Sector oldSector = sectorRepository.findOne(sectorId);
            oldSector.setSectorName(name);
            oldSector.setBrief(brief);
            res = sectorRepository.save(oldSector);
        }
        catch (NumberFormatException e){
            throw new HttpMessageNotReadableException("invalid id format");
        }
        catch (NullPointerException e){
            Map<String, Object> params = new HashMap<>();
            params.put("sectorId", id);
            throw new EntityNotFoundException(Sector.class, params);
        }
        catch (Exception e){
            System.out.println(e.toString());
            throw new GenericException(e);
        }


        return res;
    }

    @Override
    public boolean deleteSector(String id) {
        int sectorId;

        try{
            sectorId = Integer.parseInt(id);
            sectorRepository.delete(sectorId);
        }
        catch (NumberFormatException e){
            throw new HttpMessageNotReadableException("invalid id format");
        }
        catch (NullPointerException e){
            Map<String, Object> params = new HashMap<>();
            params.put("sectorId", id);
            throw new EntityNotFoundException(Sector.class, params);
        }
        catch (Exception e){
            System.out.println(e.toString());
            throw new GenericException(e);
        }

        return true;
    }

    @Override
    public List<Sector> getSectors() {
        return sectorRepository.findAll();
    }

    @Override
    public Map<String, Object> getCompaniesBySectorId(String id) {
        int sectorId;
        Sector sector;
        Map<String, Object> res = new HashMap<>();

        try{
            sectorId  = Integer.parseInt(id);
            sector = sectorRepository.findOne(sectorId);
            res.put("sectorId", sector.getSectorId());
            res.put("sectorName", sector.getSectorName());
            res.put("brief", sector.getBrief());
            res.put("companies", sector.getCompanies());
        }
        catch (NumberFormatException e){
            throw new HttpMessageNotReadableException("invalid id format");
        }
        catch (NullPointerException e){
            Map<String, Object> params = new HashMap<>();
            params.put("sectorId", id);
            throw new EntityNotFoundException(Sector.class, params);
        }
        catch (Exception e){
            System.out.println(e.toString());
            throw new GenericException(e);
        }

        return res;
    }

    @Override
    public Map<String, Object> sectorCompanyAvgPriceOnAnyExchange(String id, String startDate, String endDate) {
        Map<String, Object> res = new HashMap<>();

        try{
            int sectorId  = Integer.parseInt(id);
            Date start = Date.valueOf(startDate);
            Date end = Date.valueOf(endDate);

            Sector sector = sectorRepository.findOne(sectorId);
            List<Map<String, Object>> companiesPrice = companyRepository.getStockPriceInDateRange(start, end, sectorId);
            Map<String, Object> highestAvg = new HashMap<>();
            Map<String, Object> highestHigh = new HashMap<>();
            Map<String, Object> lowestLow = new HashMap<>();
            double tempAvg = -1;
            float tempHigh = -1;
            float tempLow = Float.MAX_VALUE;

            for(Map<String, Object> company : companiesPrice) {

                if((double)company.get("averagePrice")>tempAvg){
                    tempAvg = (double)company.get("averagePrice");
                    highestAvg.put("companyName", company.get("companyName"));
                    highestAvg.put("stockPrice", company.get("averagePrice"));
                }

                if((float)company.get("maxPrice")>tempHigh){
                    tempHigh = (float)company.get("maxPrice");
                    highestHigh.put("companyName", company.get("companyName"));
                    highestHigh.put("stockPrice", company.get("maxPrice"));
                }

                if((float)company.get("minPrice")<tempLow){
                    tempLow = (float)company.get("minPrice");
                    lowestLow.put("companyName", company.get("companyName"));
                    lowestLow.put("stockPrice", company.get("minPrice"));

                }
            }

            res.put("sectorId", sector.getSectorId());
            res.put("sectorName", sector.getSectorName());
            res.put("brief", sector.getBrief());
            res.put("noOfCompanies", sector.getCompanies().size());
            res.put("companies", companiesPrice);
            res.put("highestAvg", highestAvg);
            res.put("highestHigh", highestHigh);
            res.put("lowestLow", lowestLow);
        }
        catch (NumberFormatException e){
            throw new HttpMessageNotReadableException("invalid id format");
        }
        catch (IllegalArgumentException e){
            throw new HttpMessageNotReadableException("invalid startDate(yyyy-mm-dd), endDate(yyyy-mm-dd) format");
        }
        catch (NullPointerException e){
            Map<String, Object> params = new HashMap<>();
            params.put("sectorId", id);
            throw new EntityNotFoundException(Sector.class, params);
        }
//        catch (Exception e){
//            System.out.println(e.toString());
//            throw new GenericException(e);
//        }

        return res;
    }
}
