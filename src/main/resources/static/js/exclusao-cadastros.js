(function () {
  const mostrarAlerta = (opcoes) => {
    if (window.Swal) {
      return window.Swal.fire(opcoes);
    }
    window.alert(opcoes.text || opcoes.title || '');
    return Promise.resolve({ isConfirmed: false });
  };

  document.addEventListener('DOMContentLoaded', function () {
    const feedback = document.getElementById('cadastro-feedback');
    if (!feedback) return;

    if (feedback.dataset.erro) {
      mostrarAlerta({
        icon: 'warning',
        title: 'Exclusão não permitida',
        text: feedback.dataset.erro,
        confirmButtonText: 'Entendi',
        confirmButtonColor: '#166298'
      });
    } else if (feedback.dataset.sucesso) {
      mostrarAlerta({
        icon: 'success',
        title: 'Cadastro excluído',
        text: feedback.dataset.sucesso,
        confirmButtonText: 'OK',
        confirmButtonColor: '#166298'
      });
    }
  });

  document.addEventListener('submit', function (evento) {
    const formulario = evento.target.closest('.formulario-exclusao-cadastro');
    if (!formulario || formulario.dataset.confirmado === 'true') return;

    evento.preventDefault();
    const nome = formulario.dataset.nome || 'este cadastro';
    const mensagem = `Deseja excluir ${nome}? Esta ação não poderá ser desfeita.`;

    if (!window.Swal) {
      if (window.confirm(mensagem)) {
        formulario.dataset.confirmado = 'true';
        HTMLFormElement.prototype.submit.call(formulario);
      }
      return;
    }

    mostrarAlerta({
      icon: 'warning',
      title: 'Confirmar exclusão?',
      text: mensagem,
      showCancelButton: true,
      confirmButtonText: 'Sim, excluir',
      cancelButtonText: 'Cancelar',
      confirmButtonColor: '#dc3545',
      cancelButtonColor: '#6c757d',
      reverseButtons: true
    }).then(function (resultado) {
      if (resultado.isConfirmed) {
        formulario.dataset.confirmado = 'true';
        HTMLFormElement.prototype.submit.call(formulario);
      }
    });
  });

  document.addEventListener('click', function (evento) {
    const botao = evento.target.closest('.btn-cadastro-vinculado');
    if (!botao) return;

    const quantidade = Number(botao.dataset.vinculos || 0);
    const cadastro = botao.dataset.nome || 'Este cadastro';
    mostrarAlerta({
      icon: 'info',
      title: 'Cadastro em uso',
      text: `${cadastro} possui vínculo com ${quantidade} processo${quantidade === 1 ? '' : 's'} e não pode ser excluído.`,
      confirmButtonText: 'Entendi',
      confirmButtonColor: '#166298'
    });
  });
})();
