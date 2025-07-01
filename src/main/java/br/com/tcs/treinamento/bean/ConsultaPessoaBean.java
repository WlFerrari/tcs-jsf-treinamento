package br.com.tcs.treinamento.bean;

import br.com.tcs.treinamento.entity.Pessoa;
import br.com.tcs.treinamento.model.EmailEnviado;
import br.com.tcs.treinamento.service.PessoaService;
import br.com.tcs.treinamento.service.impl.PessoaServiceImpl;
import org.primefaces.PrimeFaces;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javax.faces.context.ExternalContext;


import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.text.SimpleDateFormat; // CORREÇÃO: Import necessário para formatar datas
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;

@ManagedBean(name = "consultaPessoaBean")
@ViewScoped
public class ConsultaPessoaBean implements Serializable {

    // ... Suas variáveis de instância continuam as mesmas ...
    private List<Pessoa> pessoas;
    private Pessoa pessoaSelecionada;
    private String errorMessage;
    private Long pessoaId;
    private Boolean tpManutencao;
    private String assuntoEmail;
    private String mensagemEmail;
    private List<EmailEnviado> emailsEnviados = new ArrayList<>();
    private transient PessoaService pessoaService = new PessoaServiceImpl();


    // ... Os métodos init(), prepararEdicao(), prepararExclusao(), etc., continuam os mesmos ...
    @PostConstruct
    public void init() {
        // Recupera parâmetro "pessoaId" da URL
        Map<String, String> params = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap();
        String idParam = params.get("pessoaId");
        if (idParam != null && !idParam.trim().isEmpty()) {
            try {
                pessoaId = Long.valueOf(idParam);
                pessoaSelecionada = pessoaService.buscarPorId(pessoaId);
            } catch (NumberFormatException e) {
                errorMessage = "ID inválido da pessoa.";
            }
        }
        // Recupera o parâmetro tpManutencao; se não existir, assume um valor padrão (por exemplo, true para edição)
        String tpParam = params.get("tpManutencao");
        if (tpParam != null && !tpParam.trim().isEmpty()) {
            setTpManutencao(Boolean.valueOf(tpParam));
        } else {
            setTpManutencao(true);
        }
        pessoas = pessoaService.listar();
    }

    public String prepararEdicao(Pessoa pessoa) {
        this.pessoaSelecionada = pessoa;
        return "alterar?faces-redirect=true&pessoaId=" + pessoa.getId() + "&tpManutencao=true";
    }

    public String prepararExclusao(Pessoa pessoa) {
        this.pessoaSelecionada = pessoa;
        return "excluir?faces-redirect=true&pessoaId=" + pessoa.getId() + "&tpManutencao=false";
    }

    public String atualizarConsulta() {
        pessoaService.atualizar(pessoaSelecionada);
        pessoas = pessoaService.listar();
        return "consultaPessoas?faces-redirect=true";
    }

    public void limparAlteracoes() {
        if (pessoaSelecionada != null) {
            pessoaSelecionada = pessoaService.buscarPorId(pessoaSelecionada.getId());
        }
    }

    public String excluirPessoa() {
        try {
            pessoaService.excluir(pessoaSelecionada);
            pessoaSelecionada = null;
            return "consultaPessoas?faces-redirect=true"; // ← volta para lista após excluir
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao excluir", e.getMessage()));
            return null;
        }
    }

    public void alternarStatus() {
        if (pessoaSelecionada != null) {
            pessoaSelecionada.setAtivo(!Boolean.TRUE.equals(pessoaSelecionada.getAtivo()));
            pessoaService.atualizar(pessoaSelecionada);
            pessoas = pessoaService.listar();
        }
    }

    public void validarCampos() {
        List<String> erros = new ArrayList<>();

        if (pessoaSelecionada.getNome() == null || pessoaSelecionada.getNome().trim().isEmpty()) {
            erros.add("Nome não informado.");
        }
        if (pessoaSelecionada.getEndereco() == null || pessoaSelecionada.getEndereco().trim().isEmpty()) {
            erros.add("Endereço não informado.");
        }

        // Adicione outras validações conforme necessidade

        if (!erros.isEmpty()) {
            errorMessage = String.join("<br/>", erros);
            PrimeFaces.current().executeScript("PF('errorDialog').show();");
        } else {
            PrimeFaces.current().executeScript("PF('confirmDialog').show();");
        }
    }

    public void confirmar() {
        try {
            pessoaService.atualizar(pessoaSelecionada);
            PrimeFaces.current().executeScript("PF('confirmDialog').hide(); PF('successDialog').show();");
        } catch (Exception e) {
            errorMessage = "Erro ao atualizar cadastro: " + e.getMessage();
            PrimeFaces.current().executeScript("PF('errorDialog').show();");
        }
    }

    public void limpar() {
        limparAlteracoes(); // reutiliza o método existente
        errorMessage = null;
    }


    public void enviarEmailComMensagem() {
        if (pessoaSelecionada == null || pessoaSelecionada.getEmail() == null || pessoaSelecionada.getEmail().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Não foi possível enviar o e-mail",
                            "Pessoa não possui e-mail cadastrado."));
            return;
        }

        if (assuntoEmail == null || assuntoEmail.trim().isEmpty() ||
                mensagemEmail == null || mensagemEmail.trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                            "Assunto e mensagem são obrigatórios", null));
            return;
        }

        // Simulação de envio
        System.out.println("Enviando e-mail para: " + pessoaSelecionada.getEmail());
        System.out.println("Assunto: " + assuntoEmail);
        System.out.println("Mensagem: " + mensagemEmail);

        // Salvar e-mail enviado
        emailsEnviados.add(new EmailEnviado(
                pessoaSelecionada.getEmail(),
                assuntoEmail,
                mensagemEmail,
                new Date()
        ));

        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "E-mail enviado com sucesso",
                        "Mensagem enviada para " + pessoaSelecionada.getEmail()));
    }

    public void verEmails() {
        PrimeFaces.current().executeScript("PF('dialogEmails').show();");
    }

    public void abrirDialogEmail(Pessoa pessoa) {
        this.pessoaSelecionada = pessoa;
        this.assuntoEmail = "";
        this.mensagemEmail = "";
    }

    /**
     * Quebra um texto em múltiplas linhas para que caiba em uma largura máxima.
     * @param text O texto completo a ser quebrado.
     * @param maxWidth A largura máxima que a linha pode ter.
     * @param font A fonte usada, para calcular a largura do texto.
     * @param fontSize O tamanho da fonte.
     * @return Uma lista de Strings, onde cada String é uma linha que cabe na largura.
     * @throws java.io.IOException
     */
    private List<String> getWrappedLines(String text, PDType1Font font, float fontSize, float maxWidth) throws java.io.IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            lines.add("");
            return lines;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            // Adiciona um espaço se a linha não estiver vazia
            String lineWithNextWord = currentLine.length() > 0 ? currentLine.toString() + " " + word : word;

            float width = font.getStringWidth(lineWithNextWord) / 1000 * fontSize;

            if (width > maxWidth) {
                // A linha atual excedeu a largura, então a salvamos
                if (currentLine.length() > 0) {
                    lines.add(currentLine.toString());
                }
                // Começamos uma nova linha com a palavra atual
                currentLine = new StringBuilder(word);
            } else {
                // A palavra cabe, então a adicionamos à linha atual
                currentLine = new StringBuilder(lineWithNextWord);
            }
        }
        // Adiciona a última linha restante
        lines.add(currentLine.toString());
        return lines;
    }

    public void exportarPdf() {
        // Usamos try-with-resources apenas para o PDDocument, que é o recurso principal.
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            externalContext.setResponseContentType("application/pdf");
            externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"pessoas_pdfbox.pdf\"");

            // CORREÇÃO: 'contentStream' é declarado fora para poder ser reatribuído.
            PDPageContentStream contentStream = null;
            try {
                contentStream = new PDPageContentStream(document, page);

                // --- Título ---
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Lista de Pessoas");
                contentStream.endText();

                // --- Configurações da Tabela ---
                float margin = 50;
                float yPosition = 720;
                float lineHeight = 15f;
                PDType1Font textFont = PDType1Font.HELVETICA;
                PDType1Font headerFont = PDType1Font.HELVETICA_BOLD;
                float fontSize = 9;

                float[] columnWidths = {100f, 130f, 30f, 130f, 70f, 70f};
                String[] headers = {"Nome", "Endereço", "Idade", "E-mail", "CPF/CNPJ", "Status"};

                // --- Desenha o Cabeçalho ---
                contentStream.setFont(headerFont, fontSize + 1);
                float xPosition = margin;
                for (int i = 0; i < headers.length; i++) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(xPosition, yPosition);
                    contentStream.showText(headers[i]);
                    contentStream.endText();
                    xPosition += columnWidths[i];
                }
                yPosition -= lineHeight;

                // --- Desenha o Conteúdo da Tabela ---
                contentStream.setFont(textFont, fontSize);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                for (Pessoa p : pessoas) {
                    String documento = "CPF".equals(p.getTipoDocumento()) ? p.getNumeroCPF() : p.getNumeroCNPJ();
                    String[] rowData = {
                            p.getNome(), p.getEndereco(), String.valueOf(p.getIdade()),
                            p.getEmail(), documento, p.getAtivo() != null && p.getAtivo() ? "Ativo" : "Inativo"
                    };

                    List<List<String>> wrappedRowData = new ArrayList<>();
                    int maxLinesInRow = 1;
                    for (int i = 0; i < rowData.length; i++) {
                        List<String> wrappedLines = getWrappedLines(rowData[i], textFont, fontSize, columnWidths[i]);
                        wrappedRowData.add(wrappedLines);
                        maxLinesInRow = Math.max(maxLinesInRow, wrappedLines.size());
                    }

                    float rowHeight = maxLinesInRow * lineHeight;

                    // Lógica para criar nova página se não houver espaço suficiente
                    if (yPosition - rowHeight < margin) {
                        contentStream.close(); // Fecha o stream da página atual
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        // CORREÇÃO: Cria um novo contentStream para a nova página
                        contentStream = new PDPageContentStream(document, page);
                        contentStream.setFont(textFont, fontSize);
                        yPosition = PDRectangle.A4.getHeight() - margin;
                    }

                    xPosition = margin;
                    for (int i = 0; i < wrappedRowData.size(); i++) {
                        float currentY = yPosition;
                        for (String line : wrappedRowData.get(i)) {
                            contentStream.beginText();
                            contentStream.newLineAtOffset(xPosition, currentY);
                            contentStream.showText(line);
                            contentStream.endText();
                            currentY -= lineHeight;
                        }
                        xPosition += columnWidths[i];
                    }
                    yPosition -= rowHeight;
                }
            } finally {
                // CORREÇÃO: Garante que o último contentStream seja fechado.
                if (contentStream != null) {
                    contentStream.close();
                }
            }

            document.save(externalContext.getResponseOutputStream());
            facesContext.responseComplete();

        } catch (Exception e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro ao exportar PDF", "Ocorreu um erro inesperado ao gerar o arquivo PDF.");
            FacesContext.getCurrentInstance().addMessage(null, message);
            e.printStackTrace();
        }
    }

    public void exportarExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Pessoas");
            // CORREÇÃO: Formatação de data para o Excel
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Nome");
            header.createCell(1).setCellValue("Endereço");
            header.createCell(2).setCellValue("Idade");
            header.createCell(3).setCellValue("E-mail");
            header.createCell(4).setCellValue("Nascimento");
            header.createCell(5).setCellValue("Cadastro");
            header.createCell(6).setCellValue("CPF/CNPJ");
            header.createCell(7).setCellValue("Status");

            int rowIdx = 1;
            for (Pessoa p : pessoas) {
                Row row = sheet.createRow(rowIdx++);
                // CORREÇÃO: Índices das células corrigidos e lógica implementada
                row.createCell(0).setCellValue(p.getNome());
                row.createCell(1).setCellValue(p.getEndereco());
                row.createCell(2).setCellValue(p.getIdade());
                row.createCell(3).setCellValue(p.getEmail());
                row.createCell(4).setCellValue(p.getData() != null ? sdf.format(p.getData()) : "");
                row.createCell(5).setCellValue(p.getDataCadastro() != null ? sdf.format(p.getDataCadastro()) : "");

                // CORREÇÃO: Lógica implementada para CPF/CNPJ
                if ("CPF".equals(p.getTipoDocumento())) {
                    row.createCell(6).setCellValue(p.getNumeroCPF());
                } else if ("CNPJ".equals(p.getTipoDocumento())) {
                    row.createCell(6).setCellValue(p.getNumeroCNPJ());
                }

                row.createCell(7).setCellValue(p.getAtivo() != null && p.getAtivo() ? "Ativo" : "Inativo");
            }

            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext externalContext = facesContext.getExternalContext();
            externalContext.setResponseContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            externalContext.setResponseHeader("Content-Disposition", "attachment; filename=\"pessoas.xlsx\"");

            workbook.write(externalContext.getResponseOutputStream());
            facesContext.responseComplete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ... Todos os getters e setters continuam os mesmos ...
    public List<Pessoa> getPessoas() {
        return pessoas;
    }

    public void setPessoas(List<Pessoa> pessoas) {
        this.pessoas = pessoas;
    }

    public Pessoa getPessoaSelecionada() {
        return pessoaSelecionada;
    }

    public void setPessoaSelecionada(Pessoa pessoaSelecionada) {
        this.pessoaSelecionada = pessoaSelecionada;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getPessoaId() {
        return pessoaId;
    }

    public void setPessoaId(Long pessoaId) {
        this.pessoaId = pessoaId;
    }

    public PessoaService getPessoaService() {
        return pessoaService;
    }

    public void setPessoaService(PessoaService pessoaService) {
        this.pessoaService = pessoaService;
    }

    public Boolean getTpManutencao() {
        return tpManutencao;
    }

    public void setTpManutencao(Boolean tpManutencao) {
        this.tpManutencao = tpManutencao;
    }

    public String getAssuntoEmail() {
        return assuntoEmail;
    }

    public void setAssuntoEmail(String assuntoEmail) {
        this.assuntoEmail = assuntoEmail;
    }

    public String getMensagemEmail() {
        return mensagemEmail;
    }



    public void setMensagemEmail(String mensagemEmail) {
        this.mensagemEmail = mensagemEmail;
    }

    public List<EmailEnviado> getEmailsEnviados() {
        return emailsEnviados;
    }

    public void setEmailsEnviados(List<EmailEnviado> emailsEnviados) {
        this.emailsEnviados = emailsEnviados;
    }
}