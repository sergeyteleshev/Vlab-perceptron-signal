const test_graph = {
    nodes: [0,1,2,3,4,5,6,7,8],
    edges: [
        [0, 1, 1, 1, 1, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 1, 1, 1, 0],
        [0, 0, 0, 0, 0, 1, 1, 1, 0],
        [0, 0, 0, 0, 0, 1, 1, 1, 0],
        [0, 0, 0, 0, 0, 1, 1, 1, 0],
        [0, 0, 0, 0, 0, 0, 0, 0, 1],
        [0, 0, 0, 0, 0, 0, 0, 0, 1],
        [0, 0, 0, 0, 0, 0, 0, 0, 1],
        [0, 0, 0, 0, 0, 0, 0, 0, 0],
    ],
    edgesBack: [
        [0, 0, 0, 0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0, 0, 0, 0],
        [0, 0, 0, 0, 0, 0, 0, 0, 0],
    ]
};

const sigmaData = {
    nodes: [
        {
            id: "n0",
            label: "A node",
            x: 0,
            y: 0,
            size: 3
        },
        {
            id: "n1",
            label: "Another node",
            x: 3,
            y: 1,
            size: 2
        },
        {
            id: "n2",
            label: "And a last one",
            x: 1,
            y: 3,
            size: 1
        }
    ],
    edges: [
        {
            id: "e0",
            source: "n0",
            target: "n1"
        },
        {
            id: "e1",
            source: "n1",
            target: "n2"
        },
        {
            id: "e2",
            source: "n2",
            target: "n0"
        }
    ]
};

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
                            <span>Алгоритм нахождения максимального потока графа на основе метода Форда-Фалкерсона</span>
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
                            <svg class="graphSvg" width=700 height=500>
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
                                    <th>№</th>
                                    <th>Пройденный путь</th>
                                    <th>Минимальный поток итерации</th>
                                </tr>                        
                                ${tableData}                                        
                            </table>  
                            <div class="step-number-input">
                                <span>Минимальный поток текущей итерации:</span>
                                <input placeholder="Минимальный вес ребра из пути" value="${templateData.currentMinWeight}" type="number" class="textInputGray"/>
                                
                                <input type="button" value="Завершить" class="btnGray completeBtn"/>
                            </div>
                            <div class="maxFlow">
                                <span>Максимальный поток графа:</span>
                                <input type='number' ${!templateData.isLabComplete ? "disabled" : ""} class='maxFlow-input' value="${templateData.maxFlow}" placeholder='Максимальный поток'/>                       
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
        selectedNodesVariantData: [[]],
        currentStep: 0,
        currentMinWeight: 0,
        currentMinWeightData: [],
        maxFlow: 0,
        isLabComplete: false,
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
    // Create the input graph
    let g = new dagreD3.graphlib.Graph()
        .setGraph({rankdir: "LR"})
        .setDefaultEdgeLabel(function () {
            return {};
        });

    state.stepsVariantData[state.currentStep].nodes.forEach((el, index) => {
        g.setNode(index, {shape: 'circle', label: el});
    });

    for (let i = 0; i < state.stepsVariantData[state.currentStep].edges.length; i++) {
        for (let j = 0; j < state.stepsVariantData[state.currentStep].edges.length; j++) {
            if (state.graphSkeleton[i][j] > 0) {
                g.setEdge(i, j, {
                    label: state.stepsVariantData[state.currentStep].edges[i][j] + "/" + state.stepsVariantData[state.currentStep].edgesBack[i][j],
                    arrowhead: "normal"
                });
            }
        }
    }

    // Create the renderer
    let render = new dagreD3.render();

    // Set up an SVG group so that we can translate the final graph.
    let svg = d3.select("svg"),
        svgGroup = svg.append("g");

    let nodesList = svg.selectAll("g.node")._groups[0];

    //очистка всех выделенных точек
    nodesList.forEach((el, index) => {
        el.setAttribute("class", el.getAttribute("class").replace("selectedNode", null));
    });

    // Run the renderer. This is what draws the final graph.
    render(d3.select("svg g"), g);

    //выделение всех нод попавших выделенные ноды
    nodesList.forEach((node, i) => {
        state.selectedNodesVariantData[state.currentStep].map((selected_node, j) => {
            if (selected_node === i) {
                node.setAttribute("class", node.getAttribute("class") + " selectedNode");
            }
        });
    });

    // Center the graph
    let xCenterOffset = (svg.attr("width") - g.graph().width) / 2;
    let yCenterOffset = (svg.attr("height") - g.graph().height) / 2;

    svgGroup.attr("transform", "translate(" + xCenterOffset + "," + yCenterOffset + ")");

    //SIGMA

    sigma.parsers.json(sigmaData, {
        container: 'container',
        settings: {
            defaultNodeColor: '#ec5148'
        }
    });

    console.log(sigmaData);
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
            if(document.getElementById("preGeneratedCode"))
            {
                if(document.getElementById("preGeneratedCode").value !== "")
                {
                    const state = appInstance.state.updateState((state) => {
                        console.log(document.getElementById("preGeneratedCode").value, 'beforeParse');
                        let graph = JSON.parse(document.getElementById("preGeneratedCode").value);
                        console.log(graph);
                        return {
                            ...state,
                            stepsVariantData: [{...graph}],
                            graphSkeleton: [...graph.edges],
                        }
                    });
                }

                //appInstance.subscriber.emit('render', state);
            }
            else
            {
                const state = appInstance.state.updateState((state) => {
                    return {
                        ...state,
                        stepsVariantData: [{...test_graph}],
                        graphSkeleton: [...test_graph.edges],
                    }
                });

                //appInstance.subscriber.emit('render', appInstance.state.getState());
            }

            const root = document.getElementById('jsLab');

            // основная функция для рендеринга
            const render = (state) => {
                console.log('state', state);
                const templateData = {
                    currentStep: state.currentStep,
                    currentMinWeight: state.currentMinWeight,
                    selectedNodesVariantData: state.selectedNodesVariantData,
                    currentMinWeightData: state.currentMinWeightData,
                    isLabComplete: state.isLabComplete,
                    maxFlow: state.maxFlow,
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