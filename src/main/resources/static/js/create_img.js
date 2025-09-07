// Esperar a que el DOM esté completamente cargado
document.addEventListener('DOMContentLoaded', function() {
    console.log("DOM cargado, inicializando funciones de archivo");
    
    const fileInput = document.getElementById('photoFile');
    const imagePreview = document.getElementById('imagePreview');
    const previewImg = document.getElementById('previewImg');

    // Verificar que los elementos existen
    console.log("Elementos encontrados:");
    console.log("fileInput:", fileInput);
    console.log("imagePreview:", imagePreview);
    console.log("previewImg:", previewImg);

    if (fileInput) {
        console.log("Agregando event listener al input file");
        
        // Agregar tanto el event listener como mantener el onclick
        fileInput.addEventListener('change', function(event) {
            console.log("Event listener change activado");
            handleFileSelectInternal(event);
        });
    } else {
        console.error("No se encontró el input file con ID 'photoFile'");
    }

    function handleFileSelectInternal(event) {
        console.log("handleFileSelectInternal llamada");
        const file = event.target.files[0];
        
        if (file) {
            console.log("Archivo detectado:", {
                name: file.name,
                type: file.type,
                size: file.size
            });
            
            if (file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    console.log("FileReader onload ejecutado");
                    try {
                        previewImg.src = e.target.result;
                        imagePreview.style.display = 'block';
                        console.log("Vista previa mostrada exitosamente");
                    } catch (error) {
                        console.error("Error al mostrar vista previa:", error);
                    }
                };
                
                reader.onerror = function(error) {
                    console.error("Error del FileReader:", error);
                };
                
                reader.readAsDataURL(file);
            } else {
                console.warn("Tipo de archivo no válido:", file.type);
                alert("Por favor selecciona un archivo de imagen válido (JPG, PNG, GIF)");
                fileInput.value = '';
            }
        } else {
            console.log("No se seleccionó ningún archivo");
        }
    }

    // Hacer las funciones globales para onclick
    window.handleFileSelect = handleFileSelectInternal;
    
    window.removeImage = function() {
        console.log("removeImage llamada");
        try {
            previewImg.src = '#';
            imagePreview.style.display = 'none';
            fileInput.value = '';
            console.log("Imagen eliminada exitosamente");
        } catch (error) {
            console.error("Error al eliminar imagen:", error);
        }
    };

    // Función para el botón examinar
    window.triggerFileInput = function() {
        console.log("triggerFileInput llamada");
        fileInput.click();
    };
});

// Funciones para manejar fechas de no disponibilidad
function addUnavailabilityDate() {
    const container = document.getElementById('unavailabilityContainer');
    const newInputGroup = document.createElement('div');
    newInputGroup.className = 'input-group mb-2';
    newInputGroup.innerHTML = `
        <input type="date" class="form-control unavailability-date" onchange="updateUnavailabilityDates()">
        <button type="button" class="btn btn-outline-danger" onclick="removeUnavailabilityDate(this)">-</button>
    `;
    container.appendChild(newInputGroup);
}

function removeUnavailabilityDate(button) {
    const container = document.getElementById('unavailabilityContainer');
    if (container.children.length > 1) {
        button.parentElement.remove();
        updateUnavailabilityDates();
    }
}

function updateUnavailabilityDates() {
    const dateInputs = document.querySelectorAll('.unavailability-date');
    const dates = [];
    
    dateInputs.forEach(input => {
        if (input.value) {
            dates.push(input.value);
        }
    });
    
    // Ordenar las fechas
    dates.sort();
    
    // Actualizar el campo hidden
    document.getElementById('unavailabilityDates').value = dates.join(',');
    
    console.log("Fechas de no disponibilidad: " + dates.join(', '));
}