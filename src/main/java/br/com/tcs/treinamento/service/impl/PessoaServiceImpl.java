package br.com.tcs.treinamento.service.impl;

import br.com.tcs.treinamento.dao.PessoaDAO;
import br.com.tcs.treinamento.entity.Pessoa;
import br.com.tcs.treinamento.service.PessoaService;

import java.util.ArrayList;
import java.util.List;

public class PessoaServiceImpl implements PessoaService {

    private PessoaDAO pessoaDAO = new PessoaDAO();
    // A lista local 'pessoas' não é necessária aqui para a operação de exclusão.
    // private List<Pessoa> pessoas = new ArrayList<>(); // Pode remover esta linha

    @Override
    public void cadastrar(Pessoa pessoa) {
        pessoaDAO.cadastrar(pessoa);
    }

    @Override
    public Pessoa buscarPorId(Long id) {
        return pessoaDAO.buscarPorId(id);
    }

    @Override
    public List<Pessoa> listar() {
        return pessoaDAO.listar();
    }

    @Override
    public void atualizar(Pessoa pessoa) {
        pessoaDAO.atualizar(pessoa);
    }

    @Override
    public void excluir(Pessoa pessoa) {
        // CORREÇÃO: Chame o DAO para excluir a pessoa do banco de dados.
        pessoaDAO.excluir(pessoa);
    }
}