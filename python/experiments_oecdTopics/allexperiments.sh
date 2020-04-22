infolder=../../graphs/oecdTopics/
outfolder=../../outputComplete/oecdTopics/

echo reading from ${infolder}
echo writing to ${outfolder}


nohup python3 -u ../experiments/compute_edge_jacc_similarities_intersection.py ${infolder} ${outfolder} > compute_edge_jacc_similarities_intersection.out &
nohup python3 -u ../experiments/compute_ged_similarities.py ${infolder} ${outfolder} > compute_ged_similarities.out &
nohup python3 -u ../experiments/compute_ged_similarities_intersection.py ${infolder} ${outfolder} > compute_ged_similarities_intersection.out &
nohup python3 -u ../experiments/compute_jacc_similarities.py ${infolder} ${outfolder} > compute_jacc_similarities.out &
nohup python3 -u ../experiments/compute_vertex_jacc_similarities.py ${infolder} ${outfolder} > compute_vertex_jacc_similarities.out &
nohup python3 -u ../experiments/compute_vertex_jacc_similarities_intersection.py ${infolder} ${outfolder} > compute_vertex_jacc_similarities_intersection.out &
nohup python3 -u ../experiments/compute_intersection_rw_kernel.py ${infolder} ${outfolder} > compute_intersection_rw_kernel.out &
nohup python3 -u ../experiments/compute_intersection_rw_kernel_unnormalized.py ${infolder} ${outfolder} > compute_intersection_rw_kernel_unnormalized.out &
nohup python3 -u ../experiments/compute_rw_similarities.py ${infolder} ${outfolder} > compute_rw_similarities.out &
nohup python3 -u ../experiments/compute_rw_similarities_intersection_lowmem.py ${infolder} ${outfolder} > compute_rw_similarities_intersection_lowmem.out &
nohup python3 -u ../experiments/compute_sp_similarities.py ${infolder} ${outfolder} > compute_sp_similarities.out &
nohup python3 -u ../experiments/compute_sp_similarities_intersection.py ${infolder} ${outfolder} > compute_sp_similarities_intersection.out &