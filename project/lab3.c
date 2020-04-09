/* Lab 3: Optimizing Program Performance
 * You should not edit this test program
 */


#include "perf.h"
#include <stdio.h>

// performs a deep copy of c1's centers into centers, and zeroes out c1's centers
int copy_clusters(cluster *c1, int64_t **centers, int num_clusters, int dimensions) {

    for (int i = 0; i < num_clusters; ++i) {
        centers[i] = (int64_t *)malloc(sizeof(int64_t) * dimensions);
        memcpy(centers[i], c1[i].center, sizeof(int64_t) * dimensions);
        memset(c1[i].center, 0, sizeof(int64_t) * dimensions);
    }
    return 0;
}

// compares sets of calculated cluster centers
int compare_results(int64_t **res1, int64_t **res2, int num_clusters, int dimensions) {
    for (int i = 0; i < num_clusters; ++i) {
        for (int j = 0; j < dimensions; ++j) {
            if (res1[i][j] != res2[i][j])
                return 0;
        }
    }
    return 1;
}

int main() {
    srand(202);
    // initialize cluster data
    int num_clusters = 256;
    int dimensions = 3;
    cluster clusters[num_clusters];
    for (int i = 0; i < num_clusters; ++i) {
        clusters[i].num_nodes = 262144;
        clusters[i].dimensions = dimensions;
        clusters[i].data = (int64_t**)malloc(sizeof(int64_t*) * dimensions);
        for (int j = 0; j < dimensions; ++j) {
            clusters[i].data[j] = (int64_t*)malloc(sizeof(int64_t) * clusters[i].num_nodes);
            for (int k = 0; k < clusters[i].num_nodes; ++k) {
                clusters[i].data[j][k] = rand();
            }
        }
        clusters[i].center = (int64_t*)malloc(sizeof(int64_t) * dimensions);
        memset(clusters[i].center, 0, sizeof(int64_t) * dimensions);
    }
    

    // calculate centers using each implementation
    naive_kmeans(clusters, num_clusters);
    int64_t **centers_1 = (int64_t **)malloc(sizeof(int64_t *) * num_clusters);
    copy_clusters(clusters, centers_1, num_clusters, dimensions);

    elim_loop_inefficiencies_and_reduce_calls_and_memrefs(clusters, num_clusters);
    int64_t **centers_2 = (int64_t **)malloc(sizeof(int64_t *) * num_clusters);
    copy_clusters(clusters, centers_2, num_clusters, dimensions);

    modest_loop_unrolling(clusters, num_clusters);
    int64_t **centers_3 = (int64_t **)malloc(sizeof(int64_t *) * num_clusters);
    copy_clusters(clusters, centers_3, num_clusters, dimensions);

    enhanced_parallelism(clusters, num_clusters);
    int64_t **centers_4 = (int64_t **)malloc(sizeof(int64_t *) * num_clusters);
    copy_clusters(clusters, centers_4, num_clusters, dimensions);


    // compare implementation results to naive implementation
    if (!compare_results(centers_1, centers_2, num_clusters, dimensions)) {
        printf("Mismatch between naive_kmeans and elim_loop_inefficiencies_and_reduce_calls_and_memrefs\n");
    }
    if (!compare_results(centers_1, centers_3, num_clusters, dimensions)) {
        printf("Mismatch between naive_kmeans and modest_loop_unrolling\n");
    }
    if (!compare_results(centers_1, centers_4, num_clusters, dimensions)) {
        printf("Mismatch between naive_kmeans and enhanced_parallelism\n");
    }
    

    return 0;
}
