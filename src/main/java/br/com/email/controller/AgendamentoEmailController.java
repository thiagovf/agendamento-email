package br.com.email.controller;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.com.email.entidade.AgendamentoEmail;
import br.com.email.servico.AgendamentoEmailServico;

@Path("emails")
public class AgendamentoEmailController {

	@Inject
	private AgendamentoEmailServico servico;
	
	@GET
	@Produces(value = MediaType.APPLICATION_JSON)
	public Response listar() {
		return Response.ok(servico.listar()).build();
	}
	
	@POST
	@Consumes(value = MediaType.APPLICATION_JSON)
	public Response inserir(AgendamentoEmail agendamentoEmail) {
		servico.inserir(agendamentoEmail);
		return Response.status(201).build();
	}
}
