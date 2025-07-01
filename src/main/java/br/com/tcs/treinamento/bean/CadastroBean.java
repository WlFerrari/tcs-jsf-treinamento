package br.com.tcs.treinamento.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.tcs.treinamento.entity.Pessoa;
import br.com.tcs.treinamento.model.PessoaVO;
import br.com.tcs.treinamento.service.PessoaService;
import br.com.tcs.treinamento.service.impl.PessoaServiceImpl;
import org.primefaces.PrimeFaces;

@ManagedBean(name="cadastroBean")
@ViewScoped
public class CadastroBean implements Serializable {
    private static final long serialVersionUID = 3450069247988201468L;

    private PessoaVO cadastrarPessoa = new PessoaVO();
    private String errorMessage;
    private transient PessoaService pessoaService = new PessoaServiceImpl();

    // Propriedade para controlar o modo de edição/revisão
    private boolean modoEdicao = true;

    /**
     * Método que converte o VO para a entidade e chama o service para persistir.
     */
    public void confirmar() {
        Pessoa pessoa = new Pessoa();
        pessoa.setNome(cadastrarPessoa.getNome());
        pessoa.setEndereco(cadastrarPessoa.getEndereco());
        pessoa.setIdade(cadastrarPessoa.getIdade());
        pessoa.setEmail(cadastrarPessoa.getEmail());
        pessoa.setData(cadastrarPessoa.getData());
        pessoa.setDataCadastro(cadastrarPessoa.getDataCadastro());
        pessoa.setTipoDocumento(cadastrarPessoa.getTipoDocumento());
        pessoa.setNumeroCPF(cadastrarPessoa.getNumeroCPF());
        pessoa.setNumeroCNPJ(cadastrarPessoa.getNumeroCNPJ());
        pessoa.setAtivo(true);

        try {
            pessoaService.cadastrar(pessoa);
            PrimeFaces.current().executeScript("PF('confirmDialog').hide(); PF('successDialog').show();");
        } catch (Exception e) {
            errorMessage = "Erro ao cadastrar pessoa: " + e.getMessage();
            PrimeFaces.current().executeScript("PF('errorDialog').show();");
        }
    }

    public void limpar() {
        cadastrarPessoa = new PessoaVO();
        errorMessage = null;
    }

    public void validarCampos() {
        List<String> erros = new ArrayList<>();

        if (cadastrarPessoa.getNome() == null || cadastrarPessoa.getNome().trim().isEmpty()) {
            erros.add("Nome não informado.");
        }
        if (cadastrarPessoa.getEndereco() == null || cadastrarPessoa.getEndereco().trim().isEmpty()) {
            erros.add("Endereço não informado.");
        }
        // Adicione outras validações aqui...

        if (!erros.isEmpty()) {
            errorMessage = String.join("<br/>", erros);
            PrimeFaces.current().executeScript("PF('errorDialog').show();");
        } else {
            // Se não houver erros, abre o diálogo de confirmação
            PrimeFaces.current().executeScript("PF('confirmDialog').show();");
        }
    }

    // Métodos para o Modo Revisão
    public boolean isModoEdicao() {
        return modoEdicao;
    }

    public void alternarModoEdicao() {
        this.modoEdicao = !this.modoEdicao;
    }

    // Getters e Setters
    public String getErrorMessage() {
        return errorMessage;
    }
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    public PessoaVO getCadastrarPessoa() {
        return cadastrarPessoa;
    }
    public void setCadastrarPessoa(PessoaVO cadastrarPessoa) {
        this.cadastrarPessoa = cadastrarPessoa;
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        this.pessoaService = new PessoaServiceImpl();
    }
}