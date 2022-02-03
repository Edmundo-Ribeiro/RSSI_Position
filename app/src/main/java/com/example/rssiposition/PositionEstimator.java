package com.example.rssiposition;


import android.util.Log;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresProblem;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.fitting.leastsquares.MultivariateJacobianFunction;
import org.apache.commons.math3.fitting.leastsquares.ParameterValidator;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.util.Pair;

import java.lang.reflect.Array;
import java.util.Set;

/**
 * Classe com o objetivo de concentrar o código referente a utilização dos dados
 * de rssi e posições dos roteadores para obter uma estimativa de posição
 */
public class PositionEstimator {
    Vector3D[] apPositions;
    String[] order;

    private void setApPositions(Vector3D[] apPositions) {
        this.apPositions = apPositions;
    }

    private void setOrder(String[] order) {
        this.order = order;
    }


    /**
     * Definir limites para as estimativas sendo geradas pela minimos quadrodos não linear
     * todo {Caso em algum momento seja possivel receber o layout
     * do ambiente sendo navegado em forma de um conjunto de vertices de um poligno.
     * Uma estrategia util seria: utilizar o algoritimo de raycasting para identificar se o
     * ponto está ou não dentro do layou (problema Point in Polygon)}
     *
     */
   private ParameterValidator checkConstrainsValidator = new ParameterValidator(){
        public RealVector validate(final RealVector point){
            RealVector aux = new ArrayRealVector(point.getDimension());
            double entry;
            final double  maxDist = 8.0;
            for(int i = 0; i < point.getDimension(); ++i){
                entry = point.getEntry(i);
                entry = Math.abs(entry);//
                entry = Math.min(entry, maxDist);
                aux.setEntry(i, entry);
            }

            return aux;
        }
    };

    //
    /**
     * Função com o modelo a ser usado para computar os residuos, retorna vetor com as atuais
     * distancias entre o {@code point} e os roteadores, para poder comparar com as distancias
     * calculadas pelos roteadores utilizando rssi. Também calcula e retorna o jacobiano.
     * A função implementada é a equação de esfera
     * o jacobiano é a derivada parcial com relação a x,y,z
     */
    private MultivariateJacobianFunction getResidualsAndJacobian = new MultivariateJacobianFunction() {

        public Pair<RealVector, RealMatrix> value(final RealVector point) {
            //obter estimativa atual da posição
            Vector3D estimatedPosition = new Vector3D(point.getEntry(0), point.getEntry(1),point.getEntry(2));

            //definir onde serão armazenados os residuos e os valores do jacobiano
            RealVector modelValues = new ArrayRealVector(apPositions.length);
            RealMatrix jacobian = new Array2DRowRealMatrix(apPositions.length, 3);

            //para cada ap disponivel
            for (int i = 0; i < apPositions.length; ++i) {

                Vector3D routerPosition = apPositions[i]; // obter posição do ap

                double model = Vector3D.distance(routerPosition, estimatedPosition); //equação r = sqrt ( (x - xo)² + (y - yo)² +(z - zo)² )
                modelValues.setEntry(i, model);

                double inverseModel = model != 0 ? (1/ model) : 0; //lidar com divisões por zero
                //derivada parcial dr/dx = (x - xo)/sqrt ( (x - xo)² + (y - yo)² +(z - zo)² )
                jacobian.setEntry(i, 0, (estimatedPosition.getX() - routerPosition.getX()) * inverseModel);
                //...
                jacobian.setEntry(i, 1, (estimatedPosition.getY() - routerPosition.getY()) * inverseModel);
                //...
                jacobian.setEntry(i, 2, (estimatedPosition.getZ() - routerPosition.getZ()) * inverseModel);

            }
            return new Pair<>(modelValues, jacobian);
        }
    };

    /**
     * Construtor vazio
     */
    public PositionEstimator(){

    }

    /**
     * Constroi, configura e realiza os calculos necessários para estimar uma posicação através dos minimos quadrados
     * @param aps objeto referente ao mapa de APs cadastrados
     * @return Vetor 3D com as coordenadas da posição estimada com base nos dados fornecidos.
     */
    public Vector3D getEstimation(APs aps){
        Set<String> keys = aps.keySet();
        this.setOrder(keys.toArray((new String[keys.size()])));

        this.setApPositions(extractApsPositions(aps));

        LeastSquaresProblem problem = setupProblem(aps);
        LeastSquaresOptimizer.Optimum optimum = new LevenbergMarquardtOptimizer().withParameterRelativeTolerance(1.0e-5).optimize(problem);
        //aps.clear(); forçar limpar aps
        return new Vector3D(optimum.getPoint().toArray());
    }

    /**
     * Configura o objeto com os dados para realizar o problema de minimos quadrados não linear
     * @param aps objeto referente ao mapa de APs cadastrados
     * @return problema de minimos quadrados construido com base nas informações extraidas de {@code aps}
     */
    private LeastSquaresProblem setupProblem(APs aps) {
        double[] initialValues = getWeightedAveragePosition(aps).toArray(); //calcular ponto inicial
        double [] targetDistances = getDistances(aps); // obter a coleção de dados que será usada de referencia no minimos quadrados
        RealMatrix weightMatrix = getWeights(aps); // obter pesos que serão aplicados aos resido nos minimos quadrados

        //Definição do problema dos minimos quadrados

        return new LeastSquaresBuilder().
                start(initialValues).
                model(getResidualsAndJacobian).
                parameterValidator(checkConstrainsValidator).
                target(targetDistances).
                weight(weightMatrix).
                lazyEvaluation(false).
                maxEvaluations(1000).
                maxIterations(1000).
                build();
    }

    /**
     * Calcula e retona vetor 3D correspondente a media ponderada das posições dos Aps contidos em {@code aps}
     * @param aps objeto referente ao mapa de APs cadastrados
     * @return vetor 3D correspondente a media ponderada das posições dos Aps
     */
    public Vector3D getWeightedAveragePosition(APs aps){

        Vector3D wavPosition = new Vector3D(0f,0f,0f);
        float weightSum = 0f;

        for(AP ap : aps.values()){
            float weight = 1/ap.rssiToDistance();
            wavPosition.add(ap.getPosition().scalarMultiply(weight));
            weightSum += weight;
        }

        return wavPosition.scalarMultiply(1/ weightSum);
    }
    /**
     * Calcula e retorna uma matriz de 3 colunas e n linhas (n sendo a quantidade de Aps inicializados com rssi).
     * essa matriz contem os pesos que serão aplicados a cada residuo no calculo dos minimos quasdrados, sendo que esse peso
     * corresponde ao inverso da distancia que o ap acredita estar do ponto em que a medida do RSSI foi feita.
     * @param aps objeto referente ao mapa de APs cadastrados
     * @return matriz de pesos de tamanho n x 3 (n = numero de Aps inicializados)
     */
    private RealMatrix getWeights(APs aps){

        RealMatrix weightMatrix = new Array2DRowRealMatrix(aps.size(), 3); //Instanciar matrix com os pesos necessários para os minimos quadrados

        float weightSum = 0f; //inicializar variavel que guardara a soma dos pesos
        double[] weights = new double[aps.size()]; //instanciar vetor que conterá o peso de cada ap

        int i = 0;
        //para cada ap
        for(String mac: order){ //garantir que os pesos estão na ordem informada na função
            weights[i] = 1/aps.get(mac).rssiToDistance(); //calcular peso
            weightSum += weights[i]; //adicionar a soma dos pesos
            i+=1;
        }
        //dividir todos os pesos pela soma (obter valores entre 0 e 1 para os pesos)
        for(int j =0; j< weights.length;++j)
            weights[j] /= weightSum;

        //colocar valores dos pesos na matrix
        weightMatrix.setColumn(0,weights); //o peso é o mesmo para x,y,z
        weightMatrix.setColumn(1,weights);
        weightMatrix.setColumn(2,weights);

        return weightMatrix;
    }

    /**
     * Obtem um array com todas as posições dos Aps inicializados, ordenados conforme {@link #order}
     * @param aps objeto referente ao mapa de APs cadastrados
     * @return array com todas as posições dos Aps inicializados
     */
    private Vector3D[] extractApsPositions(APs aps) {
        Vector3D[] positions = new Vector3D[order.length];
        int i = 0;
        for(String mac: order){
            positions[i] = aps.get(mac).getPosition();
            i+=1;
        }
        return positions;
    }

    /**
     * Obtem um array com todas as distancias calculadas pelos Aps inicializados (utilizando {@code rssiToDistance()}, ordenadas conforme {@link #order}
     * @param aps objeto referente ao mapa de APs cadastrados
     * @return array com todas as distancias calculadas pelos Aps inicializados.
     */
    private double[] getDistances(APs aps) {
        double[] distances = new double[order.length];
        int i = 0;
        for(String mac: order){
            distances[i] = aps.get(mac).rssiToDistance();
            i+=1;
        }
        return distances;
    }
}
