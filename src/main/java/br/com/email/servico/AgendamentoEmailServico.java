package br.com.email.servico;

import java.util.List;

import javax.ejb.Stateless;

import br.com.email.dao.AgendamentoEmailDAO;
import br.com.email.entidade.AgendamentoEmail;

@Stateless
public class AgendamentoEmailServico {

	public List<AgendamentoEmail> listar() {
		AgendamentoEmailDAO dao = new AgendamentoEmailDAO();
		return dao.listar();
	}
}
