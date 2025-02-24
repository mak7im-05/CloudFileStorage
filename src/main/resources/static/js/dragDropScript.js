document.querySelectorAll(".drop-zone__input").forEach((inputElement) => {
    const dropZoneElement = inputElement.closest(".drop-zone");

    dropZoneElement.addEventListener("click", () => {
        inputElement.click();
    });

    inputElement.addEventListener("change", () => {
        if (inputElement.files.length) {
            updateThumbnail(dropZoneElement, inputElement.files[0]);
        }
    });

    dropZoneElement.addEventListener("dragover", (e) => {
        e.preventDefault();
        dropZoneElement.classList.add("drop-zone--over");
    });

    ["dragleave", "dragend"].forEach((type) => {
        dropZoneElement.addEventListener(type, () => {
            dropZoneElement.classList.remove("drop-zone--over");
        });
    });

    dropZoneElement.addEventListener("drop", async (e) => {
        e.preventDefault();
        dropZoneElement.classList.remove("drop-zone--over");

        if (e.dataTransfer.files.length) {
            const files = await getFileAsync(e.dataTransfer);

            const fileList = new DataTransfer();
            files.forEach(file => fileList.items.add(file));

            inputElement.files = fileList.files;
            updateThumbnail(dropZoneElement, e.dataTransfer.files[0]);
        }
    });
});

function updateThumbnail(dropZoneElement, file) {
    let thumbnailElement = dropZoneElement.querySelector(".drop-zone__thumb");

    if (dropZoneElement.querySelector(".drop-zone__prompt")) {
        dropZoneElement.querySelector(".drop-zone__prompt").remove();
    }

    if (!thumbnailElement) {
        thumbnailElement = document.createElement("div");
        thumbnailElement.classList.add("drop-zone__thumb");
        dropZoneElement.appendChild(thumbnailElement);
    }

    thumbnailElement.dataset.label = file?.name || "files";

    if (file?.type.startsWith("image/")) {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = () => {
            thumbnailElement.style.backgroundImage = `url('${reader.result}')`;
        };
    } else {
        thumbnailElement.style.backgroundImage = null;
    }
}

async function getFileAsync(dataTransfer) {
    const files = [];
    for (let i = 0; i < dataTransfer.items.length; i++) {
        const item = dataTransfer.items[i];
        if (item.kind === 'file') {
            if (typeof item.webkitGetAsEntry === 'function') {
                const entry = item.webkitGetAsEntry();
                const entryContent = await readEntryContentAsync(entry);
                files.push(...entryContent);
                continue;
            }

            const file = item.getAsFile();
            if (file) {
                files.push(file);
            }
        }
    }
    return files;
}

function readEntryContentAsync(entry, path = '') {
    return new Promise((resolve) => {
        const contents = [];
        let reading = 0;

        function readEntry(entry, path) {
            if (entry.isFile) {
                reading++;
                entry.file(file => {
                    contents.push(new File([file], `${path}${entry.name}`, {type: file.type}));
                    reading--;
                    if (reading === 0) resolve(contents);
                });
            } else if (entry.isDirectory) {
                readReaderContent(entry.createReader(), `${path}${entry.name}/`);
            }
        }

        function readReaderContent(reader, path) {
            reading++;
            reader.readEntries(entries => {
                reading--;
                for (const entry of entries) {
                    readEntry(entry, path);
                }
                if (reading === 0) resolve(contents);
            });
        }

        readEntry(entry, path);
    });
}