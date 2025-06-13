package com.generation.blogpessoal.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.generation.blogpessoal.model.Usuario;
import com.generation.blogpessoal.repository.UsuarioRepository;
import com.generation.blogpessoal.service.UsuarioService;
import com.generation.blogpessoal.util.TestBuilder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsuarioControllerTest {

	@Autowired
	private TestRestTemplate testRestTemplate;

	@Autowired
	private UsuarioService usuarioService;

	@Autowired
	private UsuarioRepository usuarioRepository;

	private static final String USUARIO_ROOT_EMAIL = "root@root.com";
	private static final String USUARIO_ROOT_SENHA = "rootroot";
	private static final String BASE_URL_USUARIOS = "/usuarios";

	@BeforeAll
	void start() {
		usuarioRepository.deleteAll();
		usuarioService.cadastrarUsuario(TestBuilder.criarUsuarioRoot());
	}

	@Test
	@DisplayName("Deve cadastrar um novo usuário com sucesso")
	public void deveCadastrarUsuario() {

		Usuario usuario = TestBuilder.criarUsuario(null, "João Silva", "joao_silva@email.com.br", "13465278");

		HttpEntity<Usuario> requisicao = new HttpEntity<>(usuario);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(BASE_URL_USUARIOS + "/cadastrar", HttpMethod.POST,
				requisicao, Usuario.class);

		assertEquals(HttpStatus.CREATED, resposta.getStatusCode());
		assertEquals("João Silva", resposta.getBody().getNome());
		assertEquals("joao_silva@email.com.br", resposta.getBody().getUsuario());
	}

	@Test
	@DisplayName("Não deve permitir duplicação do usuário")
	public void naoDeveDuplicarUsuario() {

		Usuario usuario = TestBuilder.criarUsuario(null, "Ana Oliveira", "ana_oliveira@email.com.br", "13465278");
		usuarioService.cadastrarUsuario(usuario);

		HttpEntity<Usuario> requisicao = new HttpEntity<>(usuario);
		ResponseEntity<Usuario> resposta = testRestTemplate.exchange(BASE_URL_USUARIOS + "/cadastrar", HttpMethod.POST,
				requisicao, Usuario.class);

		assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
	}

	@Test
	@DisplayName("Deve atualizar um usuário existente")
	public void deveAtualizarUmUsuario() {

		Usuario usuario = TestBuilder.criarUsuario(null, "Fernanda Costa", "fernanda_costa@email.com.br",
				"fernanda123");
		Optional<Usuario> usuarioCadastrado = usuarioService.cadastrarUsuario(usuario);

		Usuario usuarioUpdate = TestBuilder.criarUsuario(usuarioCadastrado.get().getId(), "Fernanda Lima",
				"fernanda_lima@email.com.br", "fernanda123");

		HttpEntity<Usuario> requisicao = new HttpEntity<>(usuarioUpdate);

		ResponseEntity<Usuario> resposta = testRestTemplate.withBasicAuth(USUARIO_ROOT_EMAIL, USUARIO_ROOT_SENHA)
				.exchange(BASE_URL_USUARIOS + "/atualizar", HttpMethod.PUT, requisicao, Usuario.class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertEquals("Fernanda Lima", resposta.getBody().getNome());
		assertEquals("fernanda_lima@email.com.br", resposta.getBody().getUsuario());
	}

	@Test
	@DisplayName("Deve listar todos os usuários")
	public void deveListarTodosUsuarios() {

		usuarioService.cadastrarUsuario(TestBuilder.criarUsuario(null, "Lucas Pereira", "lucas@email.com", "senha123"));
		usuarioService
				.cadastrarUsuario(TestBuilder.criarUsuario(null, "Mariana Alves", "mariana@email.com", "senha123"));

		ResponseEntity<Usuario[]> resposta = testRestTemplate.withBasicAuth(USUARIO_ROOT_EMAIL, USUARIO_ROOT_SENHA)
				.exchange(BASE_URL_USUARIOS + "/all", HttpMethod.GET, null, Usuario[].class);

		assertEquals(HttpStatus.OK, resposta.getStatusCode());
		assertNotNull(resposta.getBody());
	}
}