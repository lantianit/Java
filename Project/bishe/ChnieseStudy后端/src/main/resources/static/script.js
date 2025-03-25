document.addEventListener('DOMContentLoaded', function() {
    const videoTableBody = document.querySelector('#video-table tbody');
    const addVideoBtn = document.getElementById('add-video-btn');
    const modal = document.getElementById('modal');
    const closeModal = document.querySelector('.close');
    const videoForm = document.getElementById('video-form');
    const videoFileInput = document.getElementById('videoFile');
    const titleInput = document.getElementById('title');
    const descriptionInput = document.getElementById('description');
    const durationInput = document.getElementById('duration');
    const videoIdInput = document.getElementById('videoId');
    const modalTitle = document.getElementById('modal-title');
    const itemsPerPage = 10;
    let currentPage = 1;
    let videos = [];

    const associateBtn = document.getElementById('associate-character-btn');
    const associateModal = document.getElementById('associate-modal');
    const closeAssociateModal = associateModal.querySelector('.close');
    const associateForm = document.getElementById('associate-form');
    const videoSearchInput = document.getElementById('video-search');
    const videoSelect = document.getElementById('video-select');

    if (!addVideoBtn ||!modal ||!closeModal ||!videoForm ||!videoFileInput) {
        console.error('One or more elements are missing from the DOM.');
        return;
    }

    // Fetch videos when the page loads
    fetchVideos();

    function fetchVideos() {
        fetch('http://127.0.0.1:8080/video/getAllVideos')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(data => {
                videos = data;
                displayVideos();
                setupPagination();
            })
            .catch(error => console.error('Error fetching videos:', error));
    }

    function displayVideos() {
        videoTableBody.innerHTML = '';
        const start = (currentPage - 1) * itemsPerPage;
        const end = start + itemsPerPage;
        const currentVideos = videos.slice(start, end);
        currentVideos.forEach(video => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${video.videoId}</td>
                <td>${video.videoUrl}</td>
                <td>${video.title}</td>
                <td>${video.duration}</td>
                <td>${video.description}</td>
                <td>${new Date(video.createTime).toLocaleString()}</td>
                <td>${new Date(video.updateTime).toLocaleString()}</td>
                <td>${video.isRecommend}</td>
                <td>${video.type}</td>
                <td>
                    <button onclick="editVideo(${video.videoId})">修改</button>
                    <button onclick="deleteVideo(${video.videoId})">删除</button>
                </td>
            `;
            videoTableBody.appendChild(row);
        });
    }

    function setupPagination() {
        const pagination = document.getElementById('pagination');
        pagination.innerHTML = '';
        const pageCount = Math.ceil(videos.length / itemsPerPage);

        const createPageButton = (page) => {
            const pageButton = document.createElement('button');
            pageButton.textContent = page;
            pageButton.onclick = () => {
                currentPage = page;
                displayVideos();
                setupPagination();
            };
            if (page === currentPage) {
                pageButton.style.fontWeight = 'bold';
            }
            return pageButton;
        };

        if (pageCount > 1) {
            pagination.appendChild(createPageButton(1));
            if (currentPage > 3) {
                const dots = document.createElement('span');
                dots.textContent = '...';
                pagination.appendChild(dots);
            }

            for (let i = Math.max(2, currentPage - 1); i <= Math.min(pageCount - 1, currentPage + 1); i++) {
                pagination.appendChild(createPageButton(i));
            }

            if (currentPage < pageCount - 2) {
                const dots = document.createElement('span');
                dots.textContent = '...';
                pagination.appendChild(dots);
            }
            pagination.appendChild(createPageButton(pageCount));
        }

        const pageInput = document.createElement('input');
        pageInput.type = 'number';
        pageInput.min = 1;
        pageInput.max = pageCount;
        pageInput.value = currentPage;
        pageInput.style.width = '50px';
        pageInput.onchange = () => {
            const page = Math.min(Math.max(1, parseInt(pageInput.value)), pageCount);
            currentPage = page;
            displayVideos();
            setupPagination();
        };
        pagination.appendChild(pageInput);

        const jumpButton = document.createElement('button');
        jumpButton.textContent = '跳转';
        jumpButton.onclick = () => {
            const page = Math.min(Math.max(1, parseInt(pageInput.value)), pageCount);
            currentPage = page;
            displayVideos();
            setupPagination();
        };
        pagination.appendChild(jumpButton);
    }

    addVideoBtn.addEventListener('click', function() {
        modal.style.display = 'block';
        videoForm.reset();
        document.getElementById('recommendNo').checked = true;
        document.getElementById('type').value = '汉字思维-李山川老师';
    });

    closeModal.addEventListener('click', function() {
        modal.style.display = 'none';
    });

    window.onclick = function(event) {
        if (event.target == modal) {
            modal.style.display = 'none';
        }
    };

    videoFileInput.addEventListener('change', function(event) {
        const file = event.target.files[0];
        if (file) {
            const fileName = file.name.split('.').slice(0, -1).join('.');
            titleInput.value = fileName;
            descriptionInput.value = fileName;

            const videoElement = document.createElement('video');
            videoElement.preload = 'metadata';
            videoElement.onloadedmetadata = function() {
                window.URL.revokeObjectURL(videoElement.src);
                const duration = videoElement.duration;
                const hours = Math.floor(duration / 3600);
                const minutes = Math.floor((duration % 3600) / 60);
                const seconds = Math.floor(duration % 60);
                durationInput.value = `${hours}h${minutes}min${seconds}sec`;
            };
            videoElement.src = URL.createObjectURL(file);
        }
    });

    videoForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const formData = new FormData(videoForm);
        const videoFile = formData.get('videoFile');
        const newVideo = {
            videoId: formData.get('videoId'),
            title: formData.get('title'),
            duration: formData.get('duration'),
            description: formData.get('description'),
            isRecommend: document.querySelector('input[name="isRecommend"]:checked').value,
            type: formData.get('type')
        };

        const uploadData = new FormData();
        if (videoFile) {
            uploadData.append('videoFile', videoFile);
        }
        uploadData.append('videoData', JSON.stringify(newVideo));

        const url = newVideo.videoId? 'http://127.0.0.1:8080/video/updateVideo' : 'http://127.0.0.1:8080/video/addVideo';

        fetch(url, {
            method: 'POST',
            body: uploadData
        })
            .then(response => response.json())
            .then(data => {
                if (data.code === 0) {
                    alert(newVideo.videoId? '修改成功' : '新增成功');
                } else {
                    alert(newVideo.videoId? '修改失败' : '新增失败');
                }
                fetchVideos();
                modal.style.display = 'none';
            })
            .catch(error => {
                console.error('Error:', error);
                alert(newVideo.videoId? '修改失败' : '新增失败');
            });
    });

    window.editVideo = function(videoId) {
        const video = videos.find(v => v.videoId === videoId);
        if (video) {
            modalTitle.textContent = '修改视频';
            videoIdInput.value = video.videoId;
            titleInput.value = video.title;
            durationInput.value = video.duration;
            descriptionInput.value = video.description;
            document.querySelector(`input[name="isRecommend"][value="${video.isRecommend}"]`).checked = true;
            document.getElementById('type').value = video.type;
            modal.style.display = 'block';
        }
    };

    window.deleteVideo = function(videoId) {
        fetch(`http://127.0.0.1:8080/video/deleteByVideoId?videoId=${videoId}`, {
            method: 'DELETE'
        })
            .then(response => response.json())
            .then(data => {
                if (data === true) {
                    alert('删除成功');
                } else {
                    alert('删除失败');
                }
                fetchVideos();
            })
            .catch(error => {
                console.error('Error deleting video:', error);
                alert('删除失败');
            });
    };

    associateBtn.addEventListener('click', function() {
        associateModal.style.display = 'block';
    });

    closeAssociateModal.addEventListener('click', function() {
        associateModal.style.display = 'none';
    });

    window.onclick = function(event) {
        if (event.target == associateModal) {
            associateModal.style.display = 'none';
        }
    };

    videoSearchInput.addEventListener('input', function() {
        const query = videoSearchInput.value;
        fetch(`http://127.0.0.1:8080/video/searchByTitle?title=${encodeURIComponent(query)}`)
            .then(response => response.json())
            .then(videos => {
                videoSelect.innerHTML = '';
                videos.forEach(video => {
                    const option = document.createElement('option');
                    option.value = video.videoId;
                    option.textContent = video.title;
                    videoSelect.appendChild(option);
                });
            });
    });

    associateForm.addEventListener('submit', function(event) {
        event.preventDefault();
        const character = document.getElementById('character').value;
        const videoId = videoSelect.value;
        const startTime = document.getElementById('start-time').value;

        fetch('http://127.0.0.1:8080/video/associateCharacter', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ character, videoId, startTime })
        })
            .then(response => response.json())
            .then(data => {
                if (data.code === 0) {
                    alert('关联成功');
                } else {
                    alert('关联失败');
                }
                associateModal.style.display = 'none';
            })
            .catch(error => {
                console.error('Error:', error);
                alert('关联失败');
            });
    });
});