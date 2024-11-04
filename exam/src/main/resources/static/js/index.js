         class ExamManager {
            constructor() {
                this.answeredQuestions = new Set();
                this.warningCount = 0;
                this.maxWarnings = 3;
                this.totalQuestions = 0;
                this.currentQuestionIndex = 0;
                this.initialize();
            }

            initialize() {
                this.setupEventListeners();
                this.initializeExam();
                this.startTimer();
            }

            setupEventListeners() {
                document.addEventListener('visibilitychange', () => this.handleVisibilityChange());
                document.addEventListener('fullscreenchange', () => this.handleFullscreenChange());
                document.addEventListener('contextmenu', (e) => e.preventDefault());
                document.addEventListener('keydown', (e) => this.handleKeyPress(e));
                window.addEventListener('blur', () => this.handleWindowBlur());
            }

            handleVisibilityChange() {
                if (document.hidden) {
                    this.handleSecurityViolation();
                }
            }

            handleFullscreenChange() {
                if (!document.fullscreenElement) {
                    this.handleSecurityViolation();
                }
            }

            handleWindowBlur() {
                this.handleSecurityViolation();
            }

            handleKeyPress(e) {
                if ((e.ctrlKey || e.metaKey) && 
                    ['c', 'p', 'u', 's', 'i'].includes(e.key.toLowerCase())) {
                    e.preventDefault();
                }

                if (e.key === 'ArrowUp' || e.key === 'ArrowDown') {
                    e.preventDefault();
                    this.navigateQuestion(e.key === 'ArrowUp' ? 'prev' : 'next');
                }
            }

            handleSecurityViolation() {
                this.warningCount++;
                this.showWarning();
            }

            showWarning() {
                const warningBanner = document.getElementById('warningBanner');
                warningBanner.style.display = 'block';
                warningBanner.textContent = `Warning ${this.warningCount}/${this.maxWarnings}: Switching tabs or minimizing window is not allowed!`;

                if (this.warningCount >= this.maxWarnings) {
                    this.autoSubmitExam();
                }

                setTimeout(() => {
                    warningBanner.style.display = 'none';
                }, 3000);
            }

            startTimer() {
                const totalTime = parseInt(document.getElementById('totalTime').value);
                let timer = totalTime;
                const display = document.querySelector('#time');
                const timerContainer = document.querySelector('.timer-container');

                const updateDisplay = () => {
                    const minutes = parseInt(timer / 60, 10);
                    const seconds = parseInt(timer % 60, 10);
                    display.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
                    
                    if (timer <= 300) {
                        timerContainer.classList.add('time-warning');
                    }

                    if (timer <= 0) {
                        clearInterval(interval);
                        this.submitExam();
                    }
                };

                updateDisplay();
                const interval = setInterval(() => {
                    timer--;
                    updateDisplay();
                }, 1000);
            }

            initializeExam() {
                const questionBlocks = document.querySelectorAll('.question-block');
                this.totalQuestions = questionBlocks.length;
                document.getElementById('totalQuestions').textContent = this.totalQuestions;

                this.initializeQuestionGrid();
                this.checkPreviousAnswers();
                this.updateProgress();
            }

            initializeQuestionGrid() {
                const grid = document.getElementById('questionGrid');
                for (let i = 0; i < this.totalQuestions; i++) {
                    const btn = document.createElement('div');
                    btn.className = 'question-number';
                    btn.textContent = i + 1;
                    btn.onclick = () => this.scrollToQuestion(i);
                    grid.appendChild(btn);
                }
            }

            checkPreviousAnswers() {
                document.querySelectorAll('.question-block').forEach((block, index) => {
                    const radio = block.querySelector('input[type="radio"]:checked');
                    if (radio) {
                        this.answeredQuestions.add(index);
                        this.updateQuestionNumber(index, true);
                    }
                });
            }

            handleOptionSelect(radio) {
                const questionBlock = radio.closest('.question-block');
                const index = Array.from(document.querySelectorAll('.question-block')).indexOf(questionBlock);
                
                this.answeredQuestions.add(index);
                this.updateQuestionNumber(index, true);
                this.updateProgress();

                const labels = questionBlock.querySelectorAll('.option-label');
                labels.forEach(label => {
                    label.classList.remove('selected');
                    if (label.contains(radio)) {
                        label.classList.add('selected');
                    }
                });
            }

            updateQuestionNumber(index, answered) {
                const numbers = document.querySelectorAll('.question-number');
                if (answered) {
                    numbers[index].classList.add('answered');
                }
            }

            updateProgress() {
                const answered = this.answeredQuestions.size;
                document.getElementById('answeredCount').textContent = answered;
                document.getElementById('progressBar').style.width = `${(answered / this.totalQuestions) * 100}%`;
            }

            scrollToQuestion(index) {
                const questionBlocks = document.querySelectorAll('.question-block');
                questionBlocks[index].scrollIntoView({ behavior: 'smooth', block: 'start' });
                
                document.querySelectorAll('.question-number').forEach((num, i) => {
                    num.classList.toggle('current', i === index);
                });
            }

            navigateQuestion(direction) {
                const questions = document.querySelectorAll('.question-block');
                
                if (direction === 'prev' && this.currentQuestionIndex > 0) {
                    this.currentQuestionIndex--;
                } else if (direction === 'next' && this.currentQuestionIndex < this.totalQuestions - 1) {
                    this.currentQuestionIndex++;
                }

                questions[this.currentQuestionIndex].scrollIntoView({ 
                    behavior: 'smooth', 
                    block: 'center' 
                });
                
                this.updateCurrentQuestion(this.currentQuestionIndex);
            }

            updateCurrentQuestion(index) {
                this.currentQuestionIndex = index;
                document.querySelectorAll('.question-number').forEach((num, i) => {
                    num.classList.toggle('current', i === index);
                });
            }

            showSubmitWarning() {
                document.getElementById('submitWarning').classList.add('show');
                document.getElementById('overlay').classList.add('show');
            }

            hideSubmitWarning() {
                document.getElementById('submitWarning').classList.remove('show');
                document.getElementById('overlay').classList.remove('show');
            }

            autoSubmitExam() {
                const securityWarning = document.createElement('div');
                securityWarning.className = 'security-warning';
                securityWarning.innerHTML = `
                    <h2>Security Violation</h2>
                    <p>Multiple attempts to switch tabs or minimize window detected.</p>
                    <p>Your exam will be submitted automatically in 10 seconds.</p>
                `;
                document.body.appendChild(securityWarning);

                setTimeout(() => {
                    this.submitExam();
                }, 10000);
            }

            submitExam() {
                document.getElementById('examForm').submit();
            }
        }

        // Initialize exam manager when the page loads
        let examManager;
        window.onload = function() {
            examManager = new ExamManager();
        };
 