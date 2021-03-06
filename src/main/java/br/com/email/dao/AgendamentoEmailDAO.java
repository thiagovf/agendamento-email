package br.com.email.dao;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import br.com.email.entidade.AgendamentoEmail;

@Stateless
public class AgendamentoEmailDAO {

	@PersistenceContext
	private EntityManager em;

	public List<AgendamentoEmail> listar() {
		return em.createQuery("SELECT ae FROM AgendamentoEmail ae", AgendamentoEmail.class).getResultList();
	}
	
	public void inserir(AgendamentoEmail agendamentoEmail) {
		em.persist(agendamentoEmail);
	}
}
