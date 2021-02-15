package br.com.email.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import br.com.email.entidade.AgendamentoEmail;

public class AgendamentoEmailDAO {

	private EntityManager em;
	
	public AgendamentoEmailDAO() {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("AgendamentoEmailDS");
		this.em = emf.createEntityManager();
	}
	
	public List<AgendamentoEmail> listar() {
		em.getTransaction().begin();
		List<AgendamentoEmail> resultado = em.createQuery("SELECT as FROM Agendamento ae",
				AgendamentoEmail.class).getResultList();
		em.getTransaction().commit();
		em.close();
		
		return resultado;
	}
}
