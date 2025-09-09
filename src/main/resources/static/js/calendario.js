// Asegurar que Bootstrap esté cargado antes de continuar
document.addEventListener('DOMContentLoaded', function() {
    console.log('🚀 DOM cargado, iniciando calendario...');
    
    // Función para esperar a que CALENDARIO_CONFIG esté disponible
    function esperarConfiguracion(intentos = 0) {
        if (window.CALENDARIO_CONFIG) {
            console.log('✅ Configuración encontrada:', window.CALENDARIO_CONFIG);
            iniciarCalendario();
        } else if (intentos < 10) {
            console.log(`⏳ Esperando configuración... intento ${intentos + 1}`);
            setTimeout(() => esperarConfiguracion(intentos + 1), 100);
        } else {
            console.error('❌ No se pudo cargar la configuración del calendario');
            // Usar valores por defecto
            window.CALENDARIO_CONFIG = {
                calendarioData: {partidos: {}, totalPartidos: 0},
                currentYear: new Date().getFullYear(),
                currentMonth: new Date().getMonth() + 1,
                arbitroId: 1,
                arbitroNombre: 'Árbitro'
            };
            console.log('🔄 Usando configuración por defecto:', window.CALENDARIO_CONFIG);
            iniciarCalendario();
        }
    }
    
    // Iniciar el proceso
    esperarConfiguracion();
    
    function iniciarCalendario() {
        const { calendarioData, currentYear, currentMonth, arbitroId, arbitroNombre } = window.CALENDARIO_CONFIG;
        
        console.log('📊 Variables del servidor:');
        console.log('  - calendarioData:', calendarioData);
        console.log('  - currentYear:', currentYear);
        console.log('  - currentMonth:', currentMonth);
        console.log('  - arbitroId:', arbitroId);
        console.log('  - arbitroNombre:', arbitroNombre);
        
        // Verificar que tenemos todo lo necesario antes de generar
        if (typeof currentYear !== 'number' || currentYear < 2000) {
            console.error('❌ currentYear inválido:', currentYear);
            return;
        }
        
        if (typeof currentMonth !== 'number' || currentMonth < 1 || currentMonth > 12) {
            console.error('❌ currentMonth inválido:', currentMonth);
            return;
        }
        
        console.log('✅ Datos válidos, iniciando tooltips...');
        
        // Inicializar tooltips de Bootstrap
        const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
        tooltipTriggerList.forEach(tooltipTriggerEl => {
            new bootstrap.Tooltip(tooltipTriggerEl);
        });
        
        console.log('✅ Generando calendario...');
        // Generar el calendario al cargar la página
        generarCalendario();
        
        function generarCalendario() {
        console.log('🔄 Iniciando generación del calendario...');
        console.log('📊 currentYear:', currentYear, 'currentMonth:', currentMonth);
        console.log('� calendarioData:', calendarioData);
        
        const grid = document.getElementById('calendarioGrid');
        if (!grid) {
            console.error('❌ Grid del calendario no encontrado');
            return;
        }
        
        console.log('✅ Grid encontrado, elementos actuales:', grid.children.length);
        
        // Limpiar días anteriores (mantener headers)
        const diasExistentes = grid.querySelectorAll('.dia-celda');
        console.log('🧹 Limpiando', diasExistentes.length, 'días existentes');
        diasExistentes.forEach(dia => dia.remove());
        
        try {
            // Calcular días del mes
            const primerDia = new Date(currentYear, currentMonth - 1, 1);
            const ultimoDia = new Date(currentYear, currentMonth, 0);
            const diasEnMes = ultimoDia.getDate();
            const primerDiaSemana = primerDia.getDay();
            
            console.log('� Primer día:', primerDia.toISOString());
            console.log('� Último día:', ultimoDia.toISOString());
            console.log('� Días en mes:', diasEnMes);
            console.log('� Primer día semana:', primerDiaSemana);
            
            const hoy = new Date();
            
            // Estadísticas
            let contadorEstadisticas = { futuros: 0, total: 0 };
            
            // Días del mes anterior
            if (primerDiaSemana > 0) {
                const mesAnterior = new Date(currentYear, currentMonth - 1, 0);
                const diasMesAnterior = mesAnterior.getDate();
                
                console.log('⬅️ Agregando', primerDiaSemana, 'días del mes anterior');
                for (let i = primerDiaSemana - 1; i >= 0; i--) {
                    const dia = diasMesAnterior - i;
                    const celda = crearCeldaDia(dia, true, false, []);
                    grid.appendChild(celda);
                }
            }
            
            // Días del mes actual
            console.log('📅 Agregando', diasEnMes, 'días del mes actual');
            for (let dia = 1; dia <= diasEnMes; dia++) {
                const fechaStr = `${currentYear}-${String(currentMonth).padStart(2, '0')}-${String(dia).padStart(2, '0')}`;
                const partidos = (calendarioData.partidos && calendarioData.partidos[fechaStr]) ? calendarioData.partidos[fechaStr] : [];
                
                const esHoy = (dia === hoy.getDate() && 
                                currentMonth === hoy.getMonth() + 1 && 
                                currentYear === hoy.getFullYear());
                
                // Contar estadísticas
                partidos.forEach(partido => {
                    contadorEstadisticas.total++;
                    if (partido.esFuturo) contadorEstadisticas.futuros++;
                });
                
                const celda = crearCeldaDia(dia, false, esHoy, partidos, fechaStr);
                grid.appendChild(celda);
            }
            
            // Completar semana
            const totalCeldas = grid.querySelectorAll('.dia-celda').length;
            const celdasNecesarias = Math.ceil(totalCeldas / 7) * 7;
            const diasSiguientes = celdasNecesarias - totalCeldas;
            
            if (diasSiguientes > 0) {
                console.log('➡️ Agregando', diasSiguientes, 'días del mes siguiente');
                for (let dia = 1; dia <= diasSiguientes; dia++) {
                    const celda = crearCeldaDia(dia, true, false, []);
                    grid.appendChild(celda);
                }
            }
            
            console.log('✅ Calendario generado - Total celdas:', grid.children.length);
            
            // Actualizar estadísticas
            const partidosFuturosEl = document.getElementById('partidosFuturos');
            const totalArbitradosEl = document.getElementById('totalArbitrados');
            if (partidosFuturosEl) partidosFuturosEl.textContent = contadorEstadisticas.futuros;
            if (totalArbitradosEl) totalArbitradosEl.textContent = contadorEstadisticas.total;
            
            // Reinicializar tooltips
            setTimeout(() => {
                const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
                tooltipTriggerList.forEach(tooltipTriggerEl => {
                    new bootstrap.Tooltip(tooltipTriggerEl);
                });
                console.log('🎯 Tooltips inicializados para', tooltipTriggerList.length, 'elementos');
            }, 100);
            
        } catch (error) {
            console.error('❌ Error generando calendario:', error);
        }
    }
    
    function crearCeldaDia(dia, esOtroMes, esHoy, partidos, fechaStr = '') {
        const celda = document.createElement('div');
        let clasesCelda = `dia-celda ${esOtroMes ? 'dia-otro-mes' : ''} ${esHoy ? 'dia-hoy' : ''}`;
        
        // Determinar tipo de día con partidos para subrayado
        if (partidos.length > 0 && !esOtroMes) {
            const tieneFuturos = partidos.some(p => p.esFuturo);
            const tienePasados = partidos.some(p => !p.esFuturo);
            
            if (tieneFuturos && tienePasados) {
                clasesCelda += ' dia-con-partidos-mixtos';
            } else if (tieneFuturos) {
                clasesCelda += ' dia-con-partidos';
            } else {
                clasesCelda += ' dia-con-partidos-pasados';
            }
            
            // Crear tooltip para días con partidos
            let tooltipContent = `${partidos.length} partido${partidos.length > 1 ? 's' : ''}:\n`;
            partidos.forEach(partido => {
                tooltipContent += `• ${partido.hora} - ${partido.equipoLocal} vs ${partido.equipoVisitante} (${partido.estado})\n`;
            });
            
            celda.setAttribute('data-bs-toggle', 'tooltip');
            celda.setAttribute('data-bs-placement', 'top');
            celda.setAttribute('data-bs-title', tooltipContent.trim());
            celda.setAttribute('data-bs-html', 'false');
        }
        
        celda.className = clasesCelda;
        
        // Crear número del día
        const numero = document.createElement('div');
        numero.className = 'dia-numero';
        numero.textContent = dia;
        celda.appendChild(numero);
        
        // Agregar partidos como elementos visuales
        partidos.forEach(partido => {
            const partidoEl = document.createElement('div');
            partidoEl.className = `partido-item ${!partido.esFuturo ? 'partido-pasado' : ''}`;
            partidoEl.textContent = `${partido.hora} - ${partido.equipoLocal} vs ${partido.equipoVisitante}`;
            partidoEl.style.cursor = 'pointer';
            partidoEl.onclick = (e) => {
                e.stopPropagation();
                mostrarDetallesPartido(partido, fechaStr);
            };
            celda.appendChild(partidoEl);
        });
        
        return celda;
    }
    
    function mostrarDetallesPartido(partido, fechaStr) {
        const modal = document.getElementById('modalPartido');
        if (!modal) {
            console.error('Modal no encontrado');
            return;
        }
        
        const modalBody = modal.querySelector('.modal-body');
        
        // Parsear la fecha del string
        const fecha = new Date(fechaStr + 'T00:00:00');
        const fechaFormateada = fecha.toLocaleDateString('es-ES', {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
        
        modalBody.innerHTML = `
            <div class="detalle-partido">
                <h5 class="text-primary mb-3">
                    <i class="fas fa-basketball-ball me-2"></i>
                    ${partido.equipoLocal} vs ${partido.equipoVisitante}
                </h5>
                
                <div class="row mb-3">
                    <div class="col-md-6">
                        <strong><i class="fas fa-calendar me-2"></i>Fecha:</strong><br>
                        <span class="text-muted">${fechaFormateada}</span>
                    </div>
                    <div class="col-md-6">
                        <strong><i class="fas fa-clock me-2"></i>Hora:</strong><br>
                        <span class="text-muted">${partido.hora}</span>
                    </div>
                </div>
                
                <div class="row mb-3">
                    <div class="col-md-12">
                        <strong><i class="fas fa-info-circle me-2"></i>Estado:</strong>
                        <span class="badge ${getEstadoBadgeClass(partido.estado)} ms-2">${partido.estado}</span>
                    </div>
                </div>
                
                <div class="row mb-3">
                    <div class="col-md-6">
                        <strong><i class="fas fa-home me-2"></i>Equipo Local:</strong><br>
                        <span class="text-primary">${partido.equipoLocal}</span>
                    </div>
                    <div class="col-md-6">
                        <strong><i class="fas fa-plane me-2"></i>Equipo Visitante:</strong><br>
                        <span class="text-danger">${partido.equipoVisitante}</span>
                    </div>
                </div>
                
                ${partido.esFuturo ? 
                    '<div class="alert alert-info"><i class="fas fa-clock me-2"></i>Partido próximo a arbitrar</div>' : 
                    '<div class="alert alert-secondary"><i class="fas fa-check-circle me-2"></i>Partido ya arbitrado</div>'
                }
            </div>
        `;
        
        const bsModal = new bootstrap.Modal(modal);
        bsModal.show();
    }
    
    function getEstadoBadgeClass(estado) {
        switch(estado) {
            case 'PROGRAMADO': return 'bg-primary';
            case 'EN_CURSO': return 'bg-warning';
            case 'FINALIZADO': return 'bg-success';
            case 'CANCELADO': return 'bg-danger';
            case 'SUSPENDIDO': return 'bg-secondary';
            default: return 'bg-secondary';
        }
    }
    
    } // Cierre de iniciarCalendario()
    
});