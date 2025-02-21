let oldFileName;
let fileType;

function removeFileExtension(filename) {
    const lastDotPosition = filename.lastIndexOf('.');
    return lastDotPosition === -1 ? filename : filename.substring(0, lastDotPosition);
}

function openRenameModal(btn, event) {
    event.preventDefault();
    const filePath = btn.getAttribute('data-current-directory') || "";
    oldFileName = btn.getAttribute('data-file-old-name');
    fileType = btn.getAttribute('data-file-type');
    document.getElementById('newFileName').value = removeFileExtension(oldFileName);
    const renameModal = document.getElementById('renameModal');
    renameModal.filePath = filePath;
    renameModal.classList.remove('hidden');
}

function closeRenameModal() {
    document.getElementById('renameModal').classList.add('hidden');
}

function submitRename() {
    const filePath = document.getElementById('renameModal').filePath;
    const newFileName = document.getElementById('newFileName').value;

    const form = document.createElement('form');
    form.method = 'post';
    form.action = buildFormAction(filePath, oldFileName, newFileName, fileType);

    document.body.appendChild(form);
    form.submit();
}

function buildFormAction(filePath, oldName, newName, type) {
    const baseAction = type === 'file' ? '/file/rename' : '/folder/rename';
    return `${baseAction}?currentDirectory=${filePath}&oldName=${oldName}&newName=${newName}`;
}