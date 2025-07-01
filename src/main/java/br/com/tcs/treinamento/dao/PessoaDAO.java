package br.com.tcs.treinamento.dao;

import br.com.tcs.treinamento.entity.Pessoa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.List;

public class PessoaDAO {

    private static EntityManagerFactory emf;

    // Cria um novo EntityManager por operação
    private EntityManager getEntityManager() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("myPU");
        }
        return emf.createEntityManager();
    }

    public void cadastrar(Pessoa pessoa) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(pessoa);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public Pessoa buscarPorId(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Pessoa.class, id);
        } finally {
            em.close();
        }
    }

    public List<Pessoa> listar() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Pessoa p", Pessoa.class).getResultList();
        } finally {
            em.close();
        }
    }

    public Pessoa atualizar(Pessoa pessoa) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Pessoa p = em.merge(pessoa);
            em.getTransaction().commit();
            return p;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void excluir(Pessoa pessoa) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Pessoa p = em.contains(pessoa) ? pessoa : em.merge(pessoa);
            em.remove(p);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
