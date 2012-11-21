package uk.ac.tgac.rampart.dao.impl;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Projections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import uk.ac.tgac.rampart.dao.AssemblyStatsDao;
import uk.ac.tgac.rampart.dao.JobDao;
import uk.ac.tgac.rampart.dao.LibraryDao;
import uk.ac.tgac.rampart.data.Job;
import uk.ac.tgac.rampart.util.RampartHibernate;

@Repository("jobDaoImpl")
public class JobDaoImpl implements JobDao {
	
	@Autowired
    private SessionFactory sessionFactory;
	
	@Autowired
	private LibraryDao libraryDao;
	
	@Autowired
	private AssemblyStatsDao assemblyStatsDao;
	
	@Override
	public Job getJob(Long id) {
		Session session = this.sessionFactory.getCurrentSession();
		Job jd = (Job)session.load(Job.class, id);
		return jd;
	}
	
	@Override
	public List<Job> getAllJobs() {	
		Session session = this.sessionFactory.getCurrentSession();
		Query q = session.createQuery("from Job");
		List<Job> jobDetails = RampartHibernate.listAndCast(q);
		return jobDetails;
	}
	
	@Override
	public long count() {
		Session session = this.sessionFactory.getCurrentSession();
		Number c = (Number) session.createCriteria(Job.class).setProjection(Projections.rowCount()).uniqueResult();
		return c.longValue();
	}
	
	@Override
	public void persist(Job job) {
		
		Session session = this.sessionFactory.getCurrentSession();
		session.saveOrUpdate(job);
	}
}