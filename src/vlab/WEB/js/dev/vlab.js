const test_graph = {
    nodes: [0,1,2,3,4,5,6,7,8],
    nodesLevel: [1, 2, 2, 2, 3, 3, 3, 3, 4],
    edges: [
        [0, 1, 1, 1, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 1, 1, 1, 1, 0],
        [0, 0, 0, 0, 1, 1, 1, 1, 0],
        [0, 0, 0, 0, 1, 1, 1, 1, 0],
        [0, 0, 0, 0, 1, 1, 1, 1, 0],
        [0, 0, 0, 0, 0, 0, 0, 0, 1],
        [0, 0, 0, 0, 0, 0, 0, 0, 1],
        [0, 0, 0, 0, 0, 0, 0, 0, 1],
        [0, 0, 0, 0, 0, 0, 0, 0, 0],
    ],
};

const test_graph_2 = {
    nodes: [0,1,2,3,4],
    nodesLevel: [1, 1, 2, 2, 3],
    nodesValue: [1, 0, null, null, null],
    edges: [
        [0, 0, 1, 1, 0],
        [0, 0, 1, 1, 0],
        [0, 0, 0, 0, 1],
        [0, 0, 0, 0, 1],
        [0, 0, 0, 0, 0],
    ],
    edgeWeight: [
        [0, 0, 0.45, 0.78, 0],
        [0, 0, -0.12, 0.13, 0],
        [0, 0, 0, 0, 1.5],
        [0, 0, 0, 0, -2.3],
        [0, 0, 0, 0, 0],
    ],
};

function dataToSigma(data) {
    let edges = data.edges;
    let nodes = data.nodes;
    let nodesLevel = data.nodesLevel;
    let edgeWeight = data.edgeWeight;
    let nodesValue = data.nodesValue;
    let resultEdges = [];
    let resultNodes = [];
    let nodesLevelAmount = [];
    let t = 1;
    let maxLevel = 1;
    let yLevel = 1;

    for (let i = 0; i < nodesLevel.length; i++) {
        nodesLevelAmount[nodesLevel[i]] = 1 + (nodesLevelAmount[nodesLevel[i]] || 0);
    }

    nodesLevelAmount.map(el => {
       if (maxLevel < el)
        maxLevel = el;
    });

    let yCenter = maxLevel / 2;

    for (let i = 0; i < nodes.length; i++) {
        let nodeValue = nodesValue[i] !== null ? `(I${i} = ${nodesValue[i]})` : "";

        //рисует всё равно криво: порядок нод не тот по вертикали, но хотя бы выравнено, лол
        if(i === 0 || i === nodes.length - 1)
        {
            yLevel = yCenter;
        }
        else
        {
            let dy = nodesLevel[i] / maxLevel;

            yLevel = i * dy;
            // if(nodesLevel[i] === nodesLevel[i - 1])
            // {
            //     yLevel = dy * t;
            //     t++;
            // }
            // else
            // {
            //     t = 1;
            // }
        }

        resultNodes[i] = {
            id: "n" + i,
            label:  `${i.toString()} ${nodeValue}`,
            x: nodesLevel[i],
            y: yLevel,
            size: 4,
            color: "#000",
        };
    }

    console.log(resultNodes);

    for (let i = 0; i < edges.length; i++) {
        for (let j = 0; j < edges.length; j++) {
            if(edges[i][j] === 1)
            {
                resultEdges.push({
                    id: "e" + i + j,
                    source: "n" + i,
                    target: "n" + j,
                    label: edgeWeight[i][j].toString(),
                });
            }
        }
    }

    return {
        nodes: resultNodes,
        edges: resultEdges,
    }
}

function getHTML(templateData) {
    let tableData = "";
    for(let i = 0; i < templateData.currentStep + 1; i++)
    {
        tableData += `<tr><td>${i+1}</td><td>{${templateData.selectedNodesVariantData[i]}}</td><td>${templateData.currentMinWeightData[i] ? templateData.currentMinWeightData[i] : ""}</td></tr>`;
    }

    return `
        <div class="lab">
            <table class="lab-table">
                <tr>
                    <td colspan="2">
                        <div class="lab-header">
                            <div></div>
                            <span>Ток сигнала в перцептроне Розенблатга</span>
                            <!-- Button trigger modal -->
                            <button type="button" class="btn btn-info" data-toggle="modal" data-target="#exampleModalScrollable">
                              Справка
                            </button>
                            
                            <!-- Modal -->
                            <div class="modal fade" id="exampleModalScrollable" tabindex="-1" role="dialog" aria-labelledby="exampleModalScrollableTitle" aria-hidden="true">
                              <div class="modal-dialog modal-dialog-scrollable" role="document">
                                <div class="modal-content">
                                  <div class="modal-header">
                                    <h5 class="modal-title" id="exampleModalScrollableTitle">Справка по интерфейсу лабораторной работы</h5>
                                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                      <span aria-hidden="true">&times;</span>
                                    </button>
                                  </div>
                                  <div class="modal-body">
                                        <p><b>Алгоритм работы с интерфейсом:</b></p>
                                        <p>
                                            1) Для того, чтобы начать строить путь из истока к стоку, нужно кликнуть на исток. Путь может начинаться только из него.                                            
                                            Далее нужно включить в текущий путь только те вершины, в которые есть ребро с <u>не</u> нулевым весом. Если вес <u>равен</u> нулю(в любую из сторон), то 
                                            лабораторная работа не позволит вам выделить эту вершину.
                                        </p>
                                        <p>
                                            2) После того как путь построен нужно в текстовом поле "минимальный поток текущей итерации" ввести то, что требуется и нажать на "+". Тем самым вы перейдёте на следующую итерацию алгоритма.
                                        </p>
                                        <p>
                                            3) Повторять шаги 2 и 3 до тех пор пока существует путь из истока к стоку.
                                        </p>
                                        <p>
                                            4) После того как путей больше нет, необходимо нажать на кнопку "завершить". Тем самым разблокируется текстовое поле "Максимальный поток графа", и можно будет ввести полученный ответ.                                        
                                        </p>
                                        <p>
                                            5) Чтобы завершить лабораторную работу, нужно нажать кнопку "отправить".
                                        </p>
                                        <p><b>Примечание:</b></p>
                                        <p>1) После ввода значений в текстовые поля кнопки не кликаются с первого раза, так как фокус остаётся на текстовом поле. Первым кликом(в любое место окна ЛР) нужно убрать фокус, а затем нажать на нужную кнопку</p>
                                        <p>2) После нажатия кнопки "завершить" весь остальной интерфейс остаётся кликабельным, так что стоит быть аккуратнее, чтобы не "сбить" результат работы.</p>
                                  </div>                                 
                                </div>
                              </div>
                            </div>                           
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>
                        <div class="graphComponent">                          
                            <div id="container"></div>
                        </div>
                    </td>
                    <td class="step-td">
                        <div class="steps">
                            <div class="steps-buttons">
                                <input class="addStep btn btn-success" type="button" value="+"/>
                                <input type="button" class="minusStep btn btn-danger" value="-">
                            </div>  
                            <table class="steps-table">
                                <tr>
                                    <th>№ нейрона</th>
                                    <th>Формула входного сигнала</th>
                                    <th>Значение входного сигнала</th>
                                    <th>Значение выходного сигнала</th>
                                </tr>                        
                                ${tableData}                                        
                            </table>  
                            <div class="step-number-input">
                                <span>Формула входного сигнала нейрона:</span>
                                <input value="${templateData.currentMinWeight}" type="text" class="textInputGray"/>
                                
                                <span>Значение входного сигнала нейрона:</span>
                                <input value="${templateData.currentMinWeight}" type="number" class="textInputGray"/>
                                
                                <span>Значение выходного сигнала нейрона:</span>
                                <input value="${templateData.currentMinWeight}" type="number" class="textInputGray"/>
                                
                                <input type="button" value="Завершить" class="btnGray completeBtn"/>
                            </div>
                            <div class="maxFlow">
                                <span>Ошибка:</span>
                                <input type='number' ${!templateData.isLabComplete ? "disabled" : ""} class='maxFlow-input' value="${templateData.maxFlow}"'/>                       
                            </div>                                                                                                                                            
                        </div>
                    </td>
                </tr>
            </table>                                                                         
        </div>`;
}

function renderTemplate(element, html) {
    element.innerHTML = html;
}

function initState() {
    let _state = {
        currentNodeSection: [],
        isLabComplete: false,
        currentSelectedNode: "",

    };

    return {
        getState: function () {
            return _state
        },
        updateState: function (callback) {
            _state = callback(_state);
            return _state;
        }
    }
}

function subscriber() {
    const events = {};

    return {
        subscribe: function (event, fn) {
            if (!events[event]) {
                events[event] = [fn]
            } else {
                events[event].push(fn);
            }

        },
        emit: function (event, data = undefined) {
            events[event].map(fn => data ? fn(data) : fn());
        }
    }
}

function App() {
    return {
        state: initState(),
        subscriber: subscriber(),
    }
}

function bindActionListeners(appInstance)
{
    document.getElementsByClassName("maxFlow-input")[0].addEventListener('change', () => {
        const state = appInstance.state.updateState((state) => {
            return {
                ...state,
                maxFlow: document.getElementsByClassName("maxFlow-input")[0].value,
            }
        });

        appInstance.subscriber.emit('render', state);
    });

    document.getElementsByClassName("completeBtn")[0].addEventListener('click', () => {
        const state = appInstance.state.updateState((state) => {
            return {
                ...state,
                isLabComplete: !state.isLabComplete,
            };
        });

        appInstance.subscriber.emit('render', state);
    });

    document.getElementsByClassName("addStep")[0].addEventListener('click', () => {
        // обновляем стейт приложение
        const state = appInstance.state.updateState((state) => {
            const currentStep = state.currentStep + 1;
            const stepsVariantData = JSON.parse(JSON.stringify(state.stepsVariantData[state.currentStep]));
            const nodesPath = state.selectedNodesVariantData[state.currentStep];
            const minEdgeWeight = state.currentMinWeight;
            const currentMinWeightData = [...state.currentMinWeightData];
            currentMinWeightData.push(minEdgeWeight);

            for (let i = 0; i < nodesPath.length - 1; i++)
            {
                if(nodesPath[i] < nodesPath[i+1])
                {
                    stepsVariantData.edges[nodesPath[i]][nodesPath[i+1]] -= +minEdgeWeight;
                    stepsVariantData.edgesBack[nodesPath[i]][nodesPath[i+1]] += +minEdgeWeight;
                }
                else
                {
                    stepsVariantData.edges[nodesPath[i+1]][nodesPath[i]] += +minEdgeWeight;
                    stepsVariantData.edgesBack[nodesPath[i+1]][nodesPath[i]] -= +minEdgeWeight;
                }
            }

            return  {
                ...state,
                currentStep,
                stepsVariantData: [...state.stepsVariantData, stepsVariantData],
                selectedNodesVariantData: [...state.selectedNodesVariantData, []],
                currentMinWeight: 0,
                currentMinWeightData,
            }
        });

        // перересовываем приложение
        appInstance.subscriber.emit('render', state);
        renderDag(state, appInstance);
    });

    document.getElementsByClassName("textInputGray")[0].addEventListener('change', () => {
        const state = appInstance.state.updateState((state) => {
            return {
                ...state,
                currentMinWeight: document.getElementsByClassName("textInputGray")[0].value,
            };
        });

        appInstance.subscriber.emit('render', state);
        renderDag(state, appInstance);
    });

    document.getElementsByClassName("minusStep")[0].addEventListener('click', () => {
        // обновляем стейт приложение
        const state = appInstance.state.updateState((state) => {
            if(state.currentStep > 0)
            {
                let stepsVariantData = JSON.parse(JSON.stringify(state.stepsVariantData));
                let selectedNodesVariantData = JSON.parse(JSON.stringify(state.selectedNodesVariantData));
                let currentMinWeightData = [...state.currentMinWeightData];
                let currentMinWeight = currentMinWeightData[currentMinWeightData.length-1];
                currentMinWeightData.pop();
                stepsVariantData.pop();
                selectedNodesVariantData.pop();

                return  {
                    ...state,
                    currentStep: state.currentStep - 1,
                    stepsVariantData,
                    selectedNodesVariantData,
                    currentMinWeightData,
                    currentMinWeight
                }
            }

            return  {
                ...state,
            }
        });

        // перересовываем приложение
        appInstance.subscriber.emit('render', state);
        renderDag(state, appInstance);
    });

    let svg = d3.select("svg");
    let nodesList = svg.selectAll("g.node")._groups[0];

    nodesList.forEach((el, index) => {
        el.addEventListener('click', () => {
            const state = appInstance.state.updateState((state) => {
                let newNodeValue = +el.textContent;
                let selectedNodesCopy = [...state.selectedNodesVariantData[state.currentStep]];

                //если точка уже в нашем пути, то удалить её, если она не разделяет наш путь на два и более несвязных путей
                if (state.selectedNodesVariantData[state.currentStep].length > 0 && state.selectedNodesVariantData[state.currentStep].includes(newNodeValue)) {
                    if (state.selectedNodesVariantData[state.currentStep][state.selectedNodesVariantData[state.currentStep].length - 1] === newNodeValue) {
                        selectedNodesCopy.splice(selectedNodesCopy.indexOf(newNodeValue), 1);
                    }
                }
                else if (state.selectedNodesVariantData[state.currentStep].length === 0 && newNodeValue === 0) // если это первый элемент, то начать новый путь
                {
                    selectedNodesCopy.push(newNodeValue);
                }
                else if (newNodeValue !== 0) //проверить, есть ли из выбранной ноды ребро в любую ноду из нашего ПУТИ
                {
                    for (let j = 0; j < state.stepsVariantData[state.currentStep].edges[newNodeValue].length; j++) {
                        if ((state.stepsVariantData[state.currentStep].edges[j][newNodeValue] > 0 && j === state.selectedNodesVariantData[state.currentStep][state.selectedNodesVariantData[state.currentStep].length - 1])) {
                            selectedNodesCopy.push(newNodeValue);
                            break;
                        } else if ((state.stepsVariantData[state.currentStep].edgesBack[newNodeValue][j] > 0 && j === state.selectedNodesVariantData[state.currentStep][state.selectedNodesVariantData[state.currentStep].length - 1])) {
                            selectedNodesCopy.push(newNodeValue);
                            break;
                        }
                    }
                }

                state.selectedNodesVariantData[state.currentStep] = JSON.parse(JSON.stringify(selectedNodesCopy));

                appInstance.subscriber.emit('render', state);
                renderDag(state,appInstance);

                return  {
                    ...state,
                };
            });
        });
    });
}

function renderDag(state, appInstance) {
    var s = new sigma({
        renderers: [{
            container: document.getElementById('container'),
            type: "canvas",
        }],
        settings: {
            defaultEdgeLabelSize: 15,
        },
    });

    let testData = dataToSigma(test_graph_2);

    testData.nodes.map(node => {
        s.graph.addNode(node);
    });

    testData.edges.map(edge => {
        s.graph.addEdge(edge);
    });

    s.bind('clickNode', (res) => {
        console.log(res);

        const state = appInstance.state.updateState((state) => {
            let currentNodeSectionCopy = [...state.currentNodeSection];
            //todo доделай блин
            if(currentNodeSectionCopy.length === 0)
            {
                currentNodeSectionCopy.push(res.data.node.id);
            }
            else if(currentNodeSectionCopy.length === 1)
            {
                currentNodeSectionCopy.map((nodeId, index) => {
                   if(nodeId === res.data.node.id)
                   {
                       currentNodeSectionCopy.splice(index, 1);
                   }
                   //в массиве 2 уже 2 элемента
                   else
                   {
                       currentNodeSectionCopy.push(res.data.node.id);
                   }
                });
            }

            return {
                ...state,
                currentNodeSection: currentNodeSectionCopy,
            }
        });

        console.log(state);
        appInstance.subscriber.emit('render', state);
    });

    s.refresh();

    console.log(testData);
}

function init_lab() {
    const appInstance = App();
    return {
        setletiant: function (str) {
        },
        setPreviosSolution: function (str) {
        },
        setMode: function (str) {
        },

        //Инициализация ВЛ
        init: function () {
            const root = document.getElementById('jsLab');

            // основная функция для рендеринга
            const render = (state) => {
                console.log('state', state);
                const templateData = {
                    isLabComplete: state.isLabComplete,
                };

                renderTemplate(root, getHTML(templateData));
                renderDag(state, appInstance);
                bindActionListeners(appInstance);
            };

            appInstance.subscriber.subscribe('render', render);

            // основная функция для рендеринга

            // инициализируем первую отрисовку
            appInstance.subscriber.emit('render', appInstance.state.getState());
        },
        getCondition: function () {
        },
        getResults: function () {
            return appInstance.state.getState();
        },
        calculateHandler: function (text, code) {
        },
    }
}

var Vlab = init_lab();