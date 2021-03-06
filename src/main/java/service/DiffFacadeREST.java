/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package service;

import com.diaam.usosm.edi.ContribsOSM;
import com.diaam.usosm.edi.entities.Diff;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 *
 * @author herve
 */
@Stateless
@Path("com.diaam.usosm.edi.entities.diff")
public class DiffFacadeREST extends AbstractFacade<Diff>
{
  @PersistenceContext(unitName = "contribsosm")
  private EntityManager em;

  public DiffFacadeREST()
  {
    super(Diff.class);
  }

  @POST
  @Override
  @Consumes(
  {
    "application/xml", "application/json"
  })
  public void create(Diff entity)
  {
    super.create(entity);
  }

  @PUT
  @Override
  @Consumes(
  {
    "application/xml", "application/json"
  })
  public void edit(Diff entity)
  {
    super.edit(entity);
  }

  @DELETE
  @Path("{id}")
  public void remove(@PathParam("id") Long id)
  {
    super.remove(super.find(id));
  }

  @GET
  @Path("{id}")
  @Produces(
  {
    "application/xml", "application/json"
  })
  public Diff find(@PathParam("id") Long id)
  {
    return super.find(id);
  }

  @GET
  @Override
  @Produces(
  {
    "application/xml", "application/json"
  })
  public List<Diff> findAll()
  {
    return super.findAll();
  }

  @GET
  @Path("{from}/{to}")
  @Produces(
  {
    "application/xml", "application/json"
  })
  public List<Diff> findRange(@PathParam("from") Integer from, @PathParam("to") Integer to)
  {
    return super.findRange(new int[]{from, to});
  }

  @GET
  @Path("count")
  @Produces("text/plain")
  public String countREST()
  {
    return String.valueOf(super.count());
  }

  @Override
  protected EntityManager getEntityManager()
  {
//    return em;
    return ContribsOSM.F_entités;
  }
  
}
