package com.oracle.tac.test;

import jakarta.enterprise.context.Dependent;

import jakarta.inject.Inject;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.stream.JsonCollectors;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import com.oracle.tac.pushData.Employee;

import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.EntityManagerFactory;
import jakarta.transaction.Transactional;
import jakarta.annotation.PostConstruct;

// Implementation based on: https://stackoverflow.com/questions/65075371/how-to-upload-file-to-oracle-db-as-blob-clob-using-helidon-mp-rest-services
//
// Other Info: https://frameworks.readthedocs.io/en/latest/rest/jerseyFileUpload.html
@Dependent
@Path("/ractest")
public class DbConnect {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            DbConnect.class);
    
    @PersistenceUnit(unitName="DeptEmpUnit")
    private EntityManagerFactory emf;

    @GET
    @Path("/getEmps")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEmps( @QueryParam("deptno")
            final String deptno) {
        JsonArray resultList = getEmployeesForDept(deptno).stream()
                .map((u) -> {
                    return Json.createObjectBuilder()
                            .add("deptNo", u.getDepartmentNo())
                            .add("empName", u.getEName())
                            .add("salary", u.getSalary())
                            .build();
                })
                .collect(JsonCollectors.toJsonArray());
        return Response.ok(resultList, MediaType.APPLICATION_JSON).build();
    }


    @GET
    @Path("/addEmps")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addEmps() {
	try {   
           EntityManager em = emf.createEntityManager();
	   EntityTransaction entityTransaction = em.getTransaction();
	   entityTransaction.begin();
           Employee e = null;
           LOGGER.info("Adding Employees....");
	   for (int i=10000 ; i<11000; i++) {
	      e = new Employee();
	      e.setDepartmentNo ( Long.valueOf(30) );
	      e.setEName ( "PFEIFER" );
	      e.setEmpNo (Long.valueOf(i));
	      e.setJob ("ENGINEER");
	      em.persist(e);
	   }
	   entityTransaction.commit();
	   em.close();

	} catch (RuntimeException e) {
	   LOGGER.error(e.getMessage());
	   e.printStackTrace();
           return Response.status(500).build();
	}
        return Response.ok(Json.createObjectBuilder().add("action","ok").build(), MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/delEmps")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delEmps() {
       LOGGER.info("Deleting Employees....");
       try {   
	   EntityManager em = emf.createEntityManager();
           EntityTransaction entityTransaction = em.getTransaction();
	   entityTransaction.begin();

	   for (int i=10000 ; i<11000; i++) {
	       Employee managedEmp = em.find(Employee.class, Long.valueOf(i));
	       if (managedEmp != null)
	          em.remove(managedEmp);
	   }
           entityTransaction.commit();
           em.close();
	}
	catch (RuntimeException e) {
            LOGGER.error(e.getMessage());
            e.printStackTrace();
	    return Response.status(500).build();
	}
        return Response.ok(Json.createObjectBuilder().add("action","ok").build(), MediaType.APPLICATION_JSON).build();
    };

    public List<Employee> getEmployeesForDept(String departmentId) {
        String myQuery = "select e from Employee e where e.departmentNo = "+departmentId;
	EntityManager em = emf.createEntityManager();
        EntityTransaction entityTransaction = em.getTransaction();
	List<Employee> list = em.createQuery( myQuery, Employee.class)
                 .getResultList();
	em.close();
	return list;
    }

}

